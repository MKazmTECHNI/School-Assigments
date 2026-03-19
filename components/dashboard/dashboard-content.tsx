"use client";

import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  ResponsiveContainer,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
} from "recharts";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
} from "@/components/ui/chart";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  MOCK_STATS,
  MOCK_REGISTRATIONS_BY_DAY,
  MOCK_TICKET_DISTRIBUTION,
  MOCK_WORKSHOP_POPULARITY,
  formatPrice,
} from "@/lib/types";
import { Users, DollarSign, Percent, TrendingUp } from "lucide-react";

// =============================================
// Stat card data
// =============================================

const statCards = [
  {
    title: "Total Registrations",
    value: MOCK_STATS.totalRegistrations.toLocaleString(),
    icon: Users,
    description: "+12% from last week",
    trend: "up" as const,
  },
  {
    title: "Total Revenue",
    value: formatPrice(MOCK_STATS.revenue),
    icon: DollarSign,
    description: "+8% from last week",
    trend: "up" as const,
  },
  {
    title: "Workshop Fill Rate",
    value: `${MOCK_STATS.workshopFillRate}%`,
    icon: Percent,
    description: "Across all workshops",
    trend: "neutral" as const,
  },
  {
    title: "Avg. Ticket Price",
    value: formatPrice(MOCK_STATS.averageTicketPrice),
    icon: TrendingUp,
    description: "Per registration",
    trend: "up" as const,
  },
];

// =============================================
// Computed colors for Recharts (cannot use CSS vars directly)
// =============================================

const CHART_COLORS = {
  primary: "#1a6dcc",
  accent: "#1a997a",
  chart3: "#7c5cbf",
  chart4: "#e6952e",
  chart5: "#d94073",
};

export function DashboardContent() {
  return (
    <div className="flex flex-col gap-6 animate-fade-in">
      {/* Stats grid - uses container query and group */}
      <div className="@container">
        <div className="grid grid-cols-1 gap-4 @sm:grid-cols-2 @xl:grid-cols-4">
          {statCards.map((stat) => {
            const Icon = stat.icon;
            return (
              <Card
                key={stat.title}
                className="group transition-all duration-200 hover:shadow-md hover:-translate-y-0.5"
              >
                <CardHeader className="flex flex-row items-center justify-between pb-2">
                  <CardTitle className="text-sm font-medium text-muted-foreground">
                    {stat.title}
                  </CardTitle>
                  <div className="flex h-8 w-8 items-center justify-center rounded-md bg-muted transition-colors group-hover:bg-primary/10">
                    <Icon className="h-4 w-4 text-muted-foreground transition-colors group-hover:text-primary" />
                  </div>
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold text-foreground">{stat.value}</div>
                  <p className="text-xs text-muted-foreground mt-1">{stat.description}</p>
                </CardContent>
              </Card>
            );
          })}
        </div>
      </div>

      {/* Charts section with tabs */}
      <Tabs defaultValue="registrations" className="w-full">
        <TabsList className="mb-4">
          <TabsTrigger value="registrations">Registration Trends</TabsTrigger>
          <TabsTrigger value="tickets">Ticket Distribution</TabsTrigger>
          <TabsTrigger value="workshops">Workshop Popularity</TabsTrigger>
        </TabsList>

        {/* Area Chart - Registration Trends */}
        <TabsContent value="registrations">
          <Card>
            <CardHeader>
              <CardTitle>Registration Trends</CardTitle>
              <CardDescription>
                Weekly registration count and revenue over the past 10 weeks.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <ChartContainer
                config={{
                  registrations: {
                    label: "Registrations",
                    color: CHART_COLORS.primary,
                  },
                  revenue: {
                    label: "Revenue ($)",
                    color: CHART_COLORS.accent,
                  },
                }}
                className="h-[350px] w-full"
              >
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart
                    data={MOCK_REGISTRATIONS_BY_DAY}
                    margin={{ top: 10, right: 10, left: 0, bottom: 0 }}
                  >
                    <defs>
                      <linearGradient id="fillRegistrations" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor={CHART_COLORS.primary} stopOpacity={0.3} />
                        <stop offset="95%" stopColor={CHART_COLORS.primary} stopOpacity={0} />
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" className="stroke-border/50" />
                    <XAxis dataKey="date" className="text-xs" />
                    <YAxis className="text-xs" />
                    <ChartTooltip content={<ChartTooltipContent />} />
                    <Area
                      type="monotone"
                      dataKey="registrations"
                      stroke={CHART_COLORS.primary}
                      fill="url(#fillRegistrations)"
                      strokeWidth={2}
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </ChartContainer>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Pie Chart - Ticket Distribution */}
        <TabsContent value="tickets">
          <Card>
            <CardHeader>
              <CardTitle>Ticket Distribution</CardTitle>
              <CardDescription>
                Breakdown of registrations by ticket tier.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <ChartContainer
                config={{
                  Standard: { label: "Standard", color: CHART_COLORS.primary },
                  Premium: { label: "Premium", color: CHART_COLORS.accent },
                  VIP: { label: "VIP", color: CHART_COLORS.chart4 },
                }}
                className="h-[350px] w-full"
              >
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={MOCK_TICKET_DISTRIBUTION}
                      cx="50%"
                      cy="50%"
                      innerRadius={80}
                      outerRadius={120}
                      paddingAngle={4}
                      dataKey="count"
                      nameKey="tier"
                    >
                      {MOCK_TICKET_DISTRIBUTION.map((entry, index) => {
                        const colors = [CHART_COLORS.primary, CHART_COLORS.accent, CHART_COLORS.chart4];
                        return (
                          <Cell key={`cell-${index}`} fill={colors[index % colors.length]} />
                        );
                      })}
                    </Pie>
                    <ChartTooltip
                      content={({ active, payload }: { active?: boolean; payload?: Array<{ name?: string; value?: number }> }) => {
                        if (!active || !payload?.length) return null;
                        const data = payload[0];
                        return (
                          <div className="rounded-lg border bg-background px-3 py-2 shadow-md">
                            <p className="text-sm font-medium text-foreground">{String(data.name)}</p>
                            <p className="text-sm text-muted-foreground">
                              {Number(data.value).toLocaleString()} registrations
                            </p>
                          </div>
                        );
                      }}
                    />
                  </PieChart>
                </ResponsiveContainer>
              </ChartContainer>

              {/* Legend */}
              <div className="flex items-center justify-center gap-6 mt-4">
                {MOCK_TICKET_DISTRIBUTION.map((item, i) => {
                  const colors = [CHART_COLORS.primary, CHART_COLORS.accent, CHART_COLORS.chart4];
                  return (
                    <div key={item.tier} className="flex items-center gap-2">
                      <div
                        className="h-3 w-3 rounded-sm"
                        style={{ backgroundColor: colors[i % colors.length] }}
                      />
                      <span className="text-sm text-muted-foreground">
                        {item.tier} ({item.count})
                      </span>
                    </div>
                  );
                })}
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Bar Chart - Workshop Popularity */}
        <TabsContent value="workshops">
          <Card>
            <CardHeader>
              <CardTitle>Workshop Popularity</CardTitle>
              <CardDescription>
                Current enrollment vs. capacity for each workshop.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <ChartContainer
                config={{
                  enrolled: { label: "Enrolled", color: CHART_COLORS.primary },
                  capacity: { label: "Capacity", color: CHART_COLORS.accent },
                }}
                className="h-[350px] w-full"
              >
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart
                    data={MOCK_WORKSHOP_POPULARITY}
                    margin={{ top: 10, right: 10, left: 0, bottom: 0 }}
                  >
                    <CartesianGrid strokeDasharray="3 3" className="stroke-border/50" />
                    <XAxis dataKey="name" className="text-xs" />
                    <YAxis className="text-xs" />
                    <ChartTooltip content={<ChartTooltipContent />} />
                    <Bar
                      dataKey="enrolled"
                      fill={CHART_COLORS.primary}
                      radius={[4, 4, 0, 0]}
                    />
                    <Bar
                      dataKey="capacity"
                      fill={CHART_COLORS.accent}
                      radius={[4, 4, 0, 0]}
                      opacity={0.4}
                    />
                  </BarChart>
                </ResponsiveContainer>
              </ChartContainer>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
