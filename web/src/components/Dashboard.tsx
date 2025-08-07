import React, { useEffect, useState } from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  Container,
  Paper,
} from '@mui/material';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
} from 'recharts';

const COLORS = ['#00B4D8', '#FF6B35', '#28A745', '#FFC107'];

const Dashboard: React.FC = () => {
  const [dashboardData, setDashboardData] = useState({
    totalUsers: 12543,
    activeUsers: 8432,
    totalGroups: 1834,
    totalPosts: 5621,
    userGrowth: [
      { month: 'Jan', users: 4000 },
      { month: 'Feb', users: 5200 },
      { month: 'Mar', users: 6800 },
      { month: 'Apr', users: 8100 },
      { month: 'May', users: 9500 },
      { month: 'Jun', users: 10800 },
      { month: 'Jul', users: 12543 },
    ],
    groupStats: [
      { purpose: 'DINING', count: 456 },
      { purpose: 'TRANSPORTATION', count: 342 },
      { purpose: 'SIGHTSEEING', count: 567 },
      { purpose: 'ACCOMMODATION', count: 234 },
      { purpose: 'ACTIVITY', count: 123 },
    ],
    postCategories: [
      { name: 'Travel Tips', value: 1234 },
      { name: 'Reviews', value: 987 },
      { name: 'Food', value: 654 },
      { name: 'Accommodation', value: 432 },
    ],
  });

  const StatCard: React.FC<{
    title: string;
    value: number;
    color: string;
    subtitle?: string;
  }> = ({ title, value, color, subtitle }) => (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Typography color="textSecondary" gutterBottom>
          {title}
        </Typography>
        <Typography variant="h4" component="div" sx={{ color }}>
          {value.toLocaleString()}
        </Typography>
        {subtitle && (
          <Typography color="textSecondary" variant="body2">
            {subtitle}
          </Typography>
        )}
      </CardContent>
    </Card>
  );

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" gutterBottom>
        Travel Mate 관리자 대시보드
      </Typography>
      
      {/* Stats Cards */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="전체 사용자"
            value={dashboardData.totalUsers}
            color="#00B4D8"
            subtitle="총 가입자 수"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="활성 사용자"
            value={dashboardData.activeUsers}
            color="#28A745"
            subtitle="월간 활성 사용자"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="여행 그룹"
            value={dashboardData.totalGroups}
            color="#FF6B35"
            subtitle="생성된 그룹 수"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="커뮤니티 게시글"
            value={dashboardData.totalPosts}
            color="#FFC107"
            subtitle="총 게시글 수"
          />
        </Grid>
      </Grid>

      {/* Charts */}
      <Grid container spacing={3}>
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              사용자 증가 추이
            </Typography>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={dashboardData.userGrowth}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="month" />
                <YAxis />
                <Tooltip />
                <Line
                  type="monotone"
                  dataKey="users"
                  stroke="#00B4D8"
                  strokeWidth={3}
                />
              </LineChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              게시글 카테고리
            </Typography>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={dashboardData.postCategories}
                  cx="50%"
                  cy="50%"
                  outerRadius={80}
                  dataKey="value"
                  label={({ name, percent }) =>
                    `${name} ${(percent * 100).toFixed(0)}%`
                  }
                >
                  {dashboardData.postCategories.map((entry, index) => (
                    <Cell
                      key={`cell-${index}`}
                      fill={COLORS[index % COLORS.length]}
                    />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        <Grid item xs={12}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              그룹 목적별 통계
            </Typography>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={dashboardData.groupStats}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="purpose" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="count" fill="#00B4D8" />
              </BarChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
};

export default Dashboard;