import React from 'react';
import { View } from 'react-native';
import { colors, radius, spacing } from '@/theme';
import { Label } from '@/components/ui';

interface Props {
  label?: string;
  value: Date | null;
  onChange: (value: Date | null) => void;
  clearable?: boolean;
  minimumDate?: Date;
}

/** Локальное время в формате, который понимает <input type="datetime-local">. */
function toInputValue(date: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0');
  return (
    `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}` +
    `T${pad(date.getHours())}:${pad(date.getMinutes())}`
  );
}

/**
 * Веб-версия поля даты (Metro подставляет её вместо DateTimeField.tsx при сборке PWA).
 *
 * Нативный пикер из @react-native-community/datetimepicker веб не поддерживает,
 * поэтому здесь используется штатный элемент браузера: на айфоне он открывает
 * привычный барабан iOS, на андроиде — системный диалог.
 */
export function DateTimeField({ label, value, onChange, clearable, minimumDate }: Props) {
  return (
    <View style={{ gap: spacing.xs }}>
      {label ? <Label>{label}</Label> : null}

      <View style={styles.row}>
        <input
          type="datetime-local"
          value={value ? toInputValue(value) : ''}
          min={minimumDate ? toInputValue(minimumDate) : undefined}
          onChange={(event) => {
            const raw = event.target.value;
            onChange(raw ? new Date(raw) : null);
          }}
          style={inputStyle}
        />

        {clearable && value ? (
          <button type="button" onClick={() => onChange(null)} style={clearStyle} aria-label="Очистить дату">
            ✕
          </button>
        ) : null}
      </View>
    </View>
  );
}

const styles = {
  row: { flexDirection: 'row' as const, alignItems: 'center' as const, gap: spacing.sm },
};

const inputStyle: React.CSSProperties = {
  flex: 1,
  backgroundColor: colors.bgElevated,
  border: `1px solid ${colors.border}`,
  borderRadius: radius.md,
  padding: '13px 12px',
  color: colors.text,
  fontSize: 15,
  fontFamily: 'inherit',
  colorScheme: 'dark',
  outline: 'none',
  width: '100%',
};

const clearStyle: React.CSSProperties = {
  background: 'transparent',
  border: `1px solid ${colors.border}`,
  borderRadius: radius.md,
  color: colors.textDim,
  cursor: 'pointer',
  fontSize: 14,
  padding: '12px 14px',
};
