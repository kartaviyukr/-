import React, { useEffect } from 'react';
import { Tabs } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { colors } from '@/theme';
import { useGoals } from '@/store/useGoals';
import { useQuests } from '@/store/useQuests';
import { useSession } from '@/store/useSession';

export default function TabsLayout() {
  const coupleId = useSession((s) => s.couple?.id);
  const loadAll = useQuests((s) => s.loadAll);
  const subscribe = useQuests((s) => s.subscribe);
  const loadGoals = useGoals((s) => s.loadGoals);
  const subscribeGoals = useGoals((s) => s.subscribeGoals);

  useEffect(() => {
    if (!coupleId) return;
    void loadAll();
    void loadGoals();
    const unsubscribers = [subscribe(), subscribeGoals()];
    return () => unsubscribers.forEach((off) => off());
  }, [coupleId, loadAll, subscribe, loadGoals, subscribeGoals]);

  return (
    <Tabs
      screenOptions={{
        headerStyle: { backgroundColor: colors.bg },
        headerTintColor: colors.text,
        headerTitleStyle: { fontWeight: '800' },
        tabBarStyle: { backgroundColor: colors.bgElevated, borderTopColor: colors.border },
        tabBarActiveTintColor: colors.primarySoft,
        tabBarInactiveTintColor: colors.textFaint,
        sceneStyle: { backgroundColor: colors.bg },
      }}
    >
      <Tabs.Screen
        name="index"
        options={{
          title: 'Квесты',
          tabBarIcon: ({ color, size }) => <Ionicons name="list-outline" color={color} size={size} />,
        }}
      />
      <Tabs.Screen
        name="calendar"
        options={{
          title: 'Календарь',
          tabBarIcon: ({ color, size }) => <Ionicons name="calendar-outline" color={color} size={size} />,
        }}
      />
      <Tabs.Screen
        name="story"
        options={{
          title: 'Сюжет',
          tabBarIcon: ({ color, size }) => <Ionicons name="book-outline" color={color} size={size} />,
        }}
      />
      <Tabs.Screen
        name="rewards"
        options={{
          title: 'Долги',
          tabBarIcon: ({ color, size }) => <Ionicons name="gift-outline" color={color} size={size} />,
        }}
      />
      <Tabs.Screen
        name="profile"
        options={{
          title: 'Герой',
          tabBarIcon: ({ color, size }) => <Ionicons name="person-outline" color={color} size={size} />,
        }}
      />
    </Tabs>
  );
}
