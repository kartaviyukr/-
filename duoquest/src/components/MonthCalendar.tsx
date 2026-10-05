import React, { useMemo } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors, radius, spacing } from '@/theme';
import { monthGrid, monthTitle, WEEKDAYS } from '@/lib/calendar';
import { Row } from '@/components/ui';

/** Точки под числом: что в этот день запланировано. */
export interface DayMarks {
  /** Цели: сколько всего и сколько закрыто. */
  goals: number;
  goalsDone: number;
  /** Дедлайны квестов. */
  quests: number;
  /** Есть просроченный незакрытый квест. */
  overdue: boolean;
}

interface Props {
  month: Date;
  selected: string;
  marksByDay: Record<string, DayMarks>;
  onSelect: (dayKey: string) => void;
  onChangeMonth: (month: Date) => void;
}

export function MonthCalendar({ month, selected, marksByDay, onSelect, onChangeMonth }: Props) {
  const cells = useMemo(() => monthGrid(month), [month]);

  return (
    <View style={styles.card}>
      <Row style={{ justifyContent: 'space-between' }}>
        <Pressable
          onPress={() => onChangeMonth(new Date(month.getFullYear(), month.getMonth() - 1, 1))}
          hitSlop={12}
          style={styles.arrow}
          accessibilityLabel="Предыдущий месяц"
        >
          <Ionicons name="chevron-back" size={20} color={colors.primarySoft} />
        </Pressable>

        <Pressable onPress={() => onChangeMonth(new Date())} hitSlop={8}>
          <Text style={styles.title}>{monthTitle(month)}</Text>
        </Pressable>

        <Pressable
          onPress={() => onChangeMonth(new Date(month.getFullYear(), month.getMonth() + 1, 1))}
          hitSlop={12}
          style={styles.arrow}
          accessibilityLabel="Следующий месяц"
        >
          <Ionicons name="chevron-forward" size={20} color={colors.primarySoft} />
        </Pressable>
      </Row>

      <View style={styles.weekRow}>
        {WEEKDAYS.map((day) => (
          <Text key={day} style={styles.weekday}>
            {day}
          </Text>
        ))}
      </View>

      <View style={styles.grid}>
        {cells.map((cell) => {
          const marks = marksByDay[cell.key];
          const isSelected = cell.key === selected;
          const allGoalsDone = marks && marks.goals > 0 && marks.goalsDone === marks.goals;

          return (
            <Pressable
              key={cell.key}
              onPress={() => onSelect(cell.key)}
              style={[
                styles.cell,
                cell.isToday && styles.cellToday,
                isSelected && styles.cellSelected,
              ]}
            >
              <Text
                style={[
                  styles.day,
                  !cell.inCurrentMonth && styles.dayOtherMonth,
                  cell.isToday && styles.dayToday,
                  isSelected && styles.daySelected,
                ]}
              >
                {cell.date.getDate()}
              </Text>

              <View style={styles.dots}>
                {marks?.goals ? (
                  <View
                    style={[styles.dot, { backgroundColor: allGoalsDone ? colors.green : colors.primarySoft }]}
                  />
                ) : null}
                {marks?.quests ? (
                  <View style={[styles.dot, { backgroundColor: marks.overdue ? colors.red : colors.gold }]} />
                ) : null}
              </View>
            </Pressable>
          );
        })}
      </View>

      <Row gap={spacing.md} style={{ justifyContent: 'center', flexWrap: 'wrap' }}>
        <Legend color={colors.primarySoft} label="цели" />
        <Legend color={colors.green} label="всё закрыто" />
        <Legend color={colors.gold} label="дедлайн квеста" />
        <Legend color={colors.red} label="просрочено" />
      </Row>
    </View>
  );
}

function Legend({ color, label }: { color: string; label: string }) {
  return (
    <Row gap={5}>
      <View style={[styles.dot, { backgroundColor: color }]} />
      <Text style={styles.legend}>{label}</Text>
    </Row>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: colors.card,
    borderRadius: radius.lg,
    borderWidth: 1,
    borderColor: colors.border,
    padding: spacing.md,
    gap: spacing.sm,
  },
  arrow: {
    width: 34,
    height: 34,
    borderRadius: 17,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.bgElevated,
  },
  title: { color: colors.text, fontSize: 17, fontWeight: '800' },
  weekRow: { flexDirection: 'row' },
  weekday: {
    flex: 1,
    textAlign: 'center',
    color: colors.textFaint,
    fontSize: 11,
    fontWeight: '700',
    textTransform: 'uppercase',
  },
  grid: { flexDirection: 'row', flexWrap: 'wrap' },
  cell: {
    // Семь колонок ровно: проценты, а не фиксированная ширина.
    width: `${100 / 7}%`,
    aspectRatio: 1,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 3,
    borderRadius: radius.sm,
  },
  cellToday: { backgroundColor: colors.bgElevated },
  cellSelected: { backgroundColor: colors.primary + '33', borderWidth: 1, borderColor: colors.primary },
  day: { color: colors.text, fontSize: 15, fontWeight: '600' },
  dayOtherMonth: { color: colors.textFaint },
  dayToday: { color: colors.gold, fontWeight: '800' },
  daySelected: { color: colors.text, fontWeight: '800' },
  dots: { flexDirection: 'row', gap: 3, height: 6 },
  dot: { width: 6, height: 6, borderRadius: 3 },
  legend: { color: colors.textFaint, fontSize: 11 },
});
