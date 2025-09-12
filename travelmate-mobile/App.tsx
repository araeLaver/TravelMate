import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createStackNavigator } from '@react-navigation/stack';
import { StatusBar, StyleSheet } from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import Icon from 'react-native-vector-icons/MaterialIcons';

// Screens
import DiscoverScreen from './src/screens/DiscoverScreen';
import ChatScreen from './src/screens/ChatScreen';
import GroupsScreen from './src/screens/GroupsScreen';
import ProfileScreen from './src/screens/ProfileScreen';
import GamesScreen from './src/screens/GamesScreen';
import ShakeScreen from './src/screens/ShakeScreen';

const Tab = createBottomTabNavigator();
const Stack = createStackNavigator();

// 메인 탭 네비게이터
function MainTabs() {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        tabBarIcon: ({ focused, color, size }) => {
          let iconName: string;

          switch (route.name) {
            case 'Discover':
              iconName = 'explore';
              break;
            case 'Chat':
              iconName = 'chat';
              break;
            case 'Groups':
              iconName = 'group';
              break;
            case 'Games':
              iconName = 'sports-esports';
              break;
            case 'Profile':
              iconName = 'person';
              break;
            default:
              iconName = 'help';
          }

          return <Icon name={iconName} size={size} color={color} />;
        },
        tabBarActiveTintColor: '#667eea',
        tabBarInactiveTintColor: '#8e8e93',
        tabBarStyle: {
          backgroundColor: '#ffffff',
          borderTopColor: '#e2e8f0',
          borderTopWidth: 1,
          height: 60,
          paddingBottom: 8,
        },
        tabBarLabelStyle: {
          fontSize: 12,
          fontWeight: '500',
        },
        headerShown: false,
      })}
    >
      <Tab.Screen
        name="Discover"
        component={DiscoverScreen}
        options={{ title: '발견' }}
      />
      <Tab.Screen
        name="Chat"
        component={ChatScreen}
        options={{ title: '채팅' }}
      />
      <Tab.Screen
        name="Groups"
        component={GroupsScreen}
        options={{ title: '그룹' }}
      />
      <Tab.Screen
        name="Games"
        component={GamesScreen}
        options={{ title: '게임' }}
      />
      <Tab.Screen
        name="Profile"
        component={ProfileScreen}
        options={{ title: '프로필' }}
      />
    </Tab.Navigator>
  );
}

// 루트 스택 네비게이터
function App(): JSX.Element {
  return (
    <NavigationContainer>
      <StatusBar barStyle="light-content" backgroundColor="#667eea" />
      <Stack.Navigator
        screenOptions={{
          headerStyle: {
            backgroundColor: '#667eea',
          },
          headerTintColor: '#ffffff',
          headerTitleStyle: {
            fontWeight: 'bold',
            fontSize: 18,
          },
        }}
      >
        <Stack.Screen
          name="Main"
          component={MainTabs}
          options={{ headerShown: false }}
        />
        <Stack.Screen
          name="Shake"
          component={ShakeScreen}
          options={{
            title: '🔍 메이트 발견하기',
            headerBackground: () => (
              <LinearGradient
                colors={['#667eea', '#764ba2']}
                style={StyleSheet.absoluteFill}
              />
            ),
          }}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
}

export default App;