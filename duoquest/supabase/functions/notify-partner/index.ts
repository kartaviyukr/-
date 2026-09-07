// Отправляет push-уведомление второму участнику пары через бесплатный Expo Push Service.
// Развернуть: supabase functions deploy notify-partner
import { createClient } from 'jsr:@supabase/supabase-js@2';
import { corsHeaders, json } from '../_shared/cors.ts';

interface Body {
  couple_id: string;
  exclude_user_id?: string;
  title: string;
  body: string;
  data?: Record<string, unknown>;
}

Deno.serve(async (req) => {
  if (req.method === 'OPTIONS') return new Response('ok', { headers: corsHeaders });

  try {
    const payload = (await req.json()) as Body;
    if (!payload?.couple_id || !payload.title) return json({ error: 'couple_id и title обязательны' }, 400);

    const supabaseUrl = Deno.env.get('SUPABASE_URL')!;
    const authHeader = req.headers.get('Authorization') ?? '';

    const asUser = createClient(supabaseUrl, Deno.env.get('SUPABASE_ANON_KEY')!, {
      global: { headers: { Authorization: authHeader } },
    });
    const { data: userData } = await asUser.auth.getUser();
    if (!userData?.user) return json({ error: 'Не авторизован' }, 401);

    const { data: membership } = await asUser
      .from('couple_members')
      .select('couple_id')
      .eq('couple_id', payload.couple_id)
      .eq('user_id', userData.user.id)
      .maybeSingle();
    if (!membership) return json({ error: 'Нет доступа к этой паре' }, 403);

    const admin = createClient(supabaseUrl, Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')!);

    const { data: members } = await admin
      .from('couple_members')
      .select('user_id')
      .eq('couple_id', payload.couple_id);

    const targets = (members ?? [])
      .map((m) => m.user_id)
      .filter((id) => id !== (payload.exclude_user_id ?? userData.user.id));

    if (targets.length === 0) return json({ ok: true, sent: 0 });

    const { data: profiles } = await admin.from('profiles').select('push_token').in('id', targets);

    // Expo-токены выглядят как ExponentPushToken[...]; остальное отбрасываем.
    const tokens = (profiles ?? [])
      .map((p) => p.push_token)
      .filter((t): t is string => typeof t === 'string' && t.startsWith('Expo'));

    if (tokens.length === 0) return json({ ok: true, sent: 0, reason: 'нет push-токенов' });

    const res = await fetch('https://exp.host/--/api/v2/push/send', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
      body: JSON.stringify(
        tokens.map((to) => ({
          to,
          sound: 'default',
          title: payload.title,
          body: payload.body,
          data: payload.data ?? {},
          channelId: 'duoquest',
          priority: 'high',
        })),
      ),
    });

    const result = await res.json();
    return json({ ok: true, sent: tokens.length, result });
  } catch (e) {
    console.error('notify-partner:', e);
    return json({ error: String(e) }, 500);
  }
});
