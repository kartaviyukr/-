import 'react-native-url-polyfill/auto';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { createClient } from '@supabase/supabase-js';

/**
 * Адрес проекта и публичный ключ намеренно лежат в коде.
 *
 * Ключ publishable (прежнее название — anon) по своему устройству живёт внутри
 * приложения: любой, у кого есть APK, извлечёт его из файла. Данные защищает не
 * он, а политики RLS в базе — см. supabase/migrations/0001_init.sql. Секретный
 * ключ (sb_secret_ / service_role) сюда попадать не должен никогда.
 *
 * Переменные окружения, если заданы при сборке, имеют приоритет — так можно
 * собрать приложение на другой проект Supabase, не трогая код.
 */
const DEFAULT_URL = 'https://pyutbbkochinddroelwe.supabase.co';
const DEFAULT_PUBLISHABLE_KEY = 'sb_publishable_tluMOa9FyqU9xC3jzF15Kg_m8uO9j-V';

const url = process.env.EXPO_PUBLIC_SUPABASE_URL || DEFAULT_URL;
const anonKey = process.env.EXPO_PUBLIC_SUPABASE_ANON_KEY || DEFAULT_PUBLISHABLE_KEY;

export const isSupabaseConfigured = Boolean(url && anonKey);

export const supabase = createClient(url, anonKey, {
  auth: {
    storage: AsyncStorage,
    autoRefreshToken: true,
    persistSession: true,
    // В мобильном приложении нет URL-строки, из которой можно вынуть токен.
    detectSessionInUrl: false,
  },
});
