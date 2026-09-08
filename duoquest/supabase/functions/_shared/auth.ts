import type { SupabaseClient } from 'jsr:@supabase/supabase-js@2';

/**
 * Проверяет, что запрос пришёл от авторизованного пользователя, состоящего
 * в указанной паре.
 *
 * Клиент здесь — сервисный, он игнорирует RLS, поэтому право доступа
 * проверяется явным запросом, а не политиками. Токен разбирается тем же
 * сервисным клиентом, так что публичный ключ функции не нужен: у проектов
 * на новой системе ключей переменной SUPABASE_ANON_KEY может не быть вовсе.
 */
export async function requireCoupleMember(
  admin: SupabaseClient,
  req: Request,
  coupleId: string,
): Promise<{ id: string } | { error: string; status: number }> {
  const token = (req.headers.get('Authorization') ?? '').replace(/^Bearer\s+/i, '').trim();
  if (!token) return { error: 'Не авторизован', status: 401 };

  const { data, error } = await admin.auth.getUser(token);
  if (error || !data?.user) return { error: 'Не авторизован', status: 401 };

  const { data: membership } = await admin
    .from('couple_members')
    .select('couple_id')
    .eq('couple_id', coupleId)
    .eq('user_id', data.user.id)
    .maybeSingle();

  if (!membership) return { error: 'Нет доступа к этой паре', status: 403 };

  return { id: data.user.id };
}
