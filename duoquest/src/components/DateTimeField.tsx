import React, { useState } from 'react';
import { Platform, Pressable, StyleSheet, Text, View } from 'react-native';
import DateTimePicker, { type DateTimePickerEvent } from '@react-native-community/datetimepicker';
import { Ionicons } from '@expo/vector-icons';
import { colors, radius, spacing } from '@/theme';
import { formatDateTime } from '@/lib/dates';
import { Label } from '@/components/ui';

interface Props {
  label?: string;
  value: Date | null;
  onChange: (value: Date | null) => void;
  clearable?: boolean;
  minimumDate?: Date;
}

/**
 * Кроссплатформенный выбор даты и времени.
 * На Android системный пикер показывает дату и время двумя шагами, на iOS — одним инлайн-виджетом.
 */
export function DateTimeField({ label, value, onChange, clearable, minimumDate }: Props) {
  const [mode, setMode] = useState<'date' | 'time' | null>(null);
  const [draft, setDraft] = useState<Date | null>(null);

  const open = () => {
    setDraft(value ?? new Date());
    setMode('date');
  };

  const handle = (event: DateTimePickerEvent, picked?: Date) => {
    if (Platform.OS === 'android') {
      if (event.type === 'dismissed') {
        setMode(null);
        return;
      }
      const base = draft ?? new Date();
      if (mode === 'date' && picked) {
        const next = new Date(base);
        next.setFullYear(picked.getFullYear(), picked.getMonth(), picked.getDate());
        setDraft(next);
        setMode('time');
        return;
      }
      if (mode === 'time' && picked) {
        const next = new Date(base);
        next.setHours(picked.getHours(), picked.getMinutes(), 0, 0);
        setMode(null);
        onChange(next);
      }
      return;
    }

    // iOS: единый пикер, значение применяем сразу.
    if (picked) {
      setDraft(picked);
      onChange(picked);
    }
  };

  return (
    <View style={{ gap: spacing.xs }}>
      {label ? <Label>{label}</Label> : null}

      <Pressable onPress={open} style={styles.field}>
        <Ionicons name="calendar-outline" size={18} color={colors.primarySoft} />
        <Text style={[styles.value, !value && { color: colors.textFaint }]}>
          {value ? formatDateTime(value) : 'Выбрать дату и время'}
        </Text>
        {clearable && value ? (
          <Pressable onPress={() => onChange(null)} hitSlop={10}>
            <Ionicons name="close-circle" size={18} color={colors.textFaint} />
          </Pressable>
        ) : null}
      </Pressable>

      {mode && Platform.OS === 'android' ? (
        <DateTimePicker
          value={draft ?? new Date()}
          mode={mode}
          is24Hour
          minimumDate={minimumDate}
          onChange={handle}
        />
      ) : null}

      {mode && Platform.OS === 'ios' ? (
        <View style={styles.iosWrap}>
          <DateTimePicker
            value={draft ?? value ?? new Date()}
            mode="datetime"
            display="spinner"
            themeVariant="dark"
            locale="ru-RU"
            minimumDate={minimumDate}
            onChange={handle}
          />
          <Pressable onPress={() => setMode(null)} style={styles.iosDone}>
            <Text style={styles.iosDoneText}>Готово</Text>
          </Pressable>
        </View>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  field: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
    backgroundColor: colors.bgElevated,
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: radius.md,
    paddingHorizontal: spacing.md,
    paddingVertical: 13,
  },
  value: { flex: 1, color: colors.text, fontSize: 15 },
  iosWrap: {
    backgroundColor: colors.bgElevated,
    borderRadius: radius.md,
    borderWidth: 1,
    borderColor: colors.border,
    overflow: 'hidden',
  },
  iosDone: { paddingVertical: 12, alignItems: 'center', backgroundColor: colors.primary },
  iosDoneText: { color: '#fff', fontWeight: '700' },
});
