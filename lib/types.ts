// =============================================
// TypeScript: Union types, intersection types
// =============================================

export type TicketTier = "standard" | "premium" | "vip";
export type DietaryPreference = "none" | "vegetarian" | "vegan" | "gluten-free" | "halal";
export type RegistrationStatus = "pending" | "confirmed" | "cancelled";

export type Workshop = {
  id: string;
  title: string;
  speaker: string;
  description: string;
  capacity: number;
  enrolled: number;
  time: string;
  track: "frontend" | "backend" | "devops" | "ai";
};

export type PersonalInfo = {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  company: string;
  jobTitle: string;
};

export type TicketInfo = {
  tier: TicketTier;
  dietaryPreference: DietaryPreference;
  accessibilityNeeds: string;
  tShirtSize: "xs" | "s" | "m" | "l" | "xl" | "xxl";
};

export type WorkshopPreferences = {
  selectedWorkshops: string[];
  specialRequests: string;
};

// Intersection type: full registration data
export type RegistrationData = PersonalInfo & TicketInfo & WorkshopPreferences;

// =============================================
// TypeScript: Built-in generic types (Pick, Omit, Partial, Record, Readonly)
// =============================================

export type RegistrationSummary = Pick<PersonalInfo, "firstName" | "lastName" | "email"> &
  Pick<TicketInfo, "tier">;

export type OptionalPersonalInfo = Partial<PersonalInfo>;

export type TicketPricing = Readonly<Record<TicketTier, number>>;

export type RegistrationWithoutWorkshops = Omit<RegistrationData, "selectedWorkshops" | "specialRequests">;

// =============================================
// TypeScript: Type predicates
// =============================================

export function isTicketTier(value: unknown): value is TicketTier {
  return typeof value === "string" && ["standard", "premium", "vip"].includes(value);
}

export function isRegistrationComplete(
  data: Partial<RegistrationData>
): data is RegistrationData {
  return (
    typeof data.firstName === "string" &&
    typeof data.lastName === "string" &&
    typeof data.email === "string" &&
    typeof data.phone === "string" &&
    typeof data.tier === "string" &&
    isTicketTier(data.tier) &&
    Array.isArray(data.selectedWorkshops)
  );
}

// =============================================
// TypeScript: Function overloads
// =============================================

export function formatPrice(amount: number): string;
export function formatPrice(amount: number, currency: string): string;
export function formatPrice(amount: number, currency: string, locale: string): string;
export function formatPrice(amount: number, currency = "USD", locale = "en-US"): string {
  return new Intl.NumberFormat(locale, {
    style: "currency",
    currency,
  }).format(amount);
}

// =============================================
// Ticket pricing data
// =============================================

export const TICKET_PRICES: TicketPricing = {
  standard: 299,
  premium: 599,
  vip: 999,
} as const;

export const WORKSHOPS: Workshop[] = [
  {
    id: "ws-1",
    title: "Building with Next.js 16",
    speaker: "Sarah Chen",
    description: "Deep dive into the latest features of Next.js 16 including cache components and improved routing.",
    capacity: 50,
    enrolled: 38,
    time: "9:00 AM - 12:00 PM",
    track: "frontend",
  },
  {
    id: "ws-2",
    title: "Advanced TypeScript Patterns",
    speaker: "Marcus Reid",
    description: "Master generics, conditional types, and type-level programming for production applications.",
    capacity: 40,
    enrolled: 35,
    time: "9:00 AM - 12:00 PM",
    track: "frontend",
  },
  {
    id: "ws-3",
    title: "Kubernetes at Scale",
    speaker: "Priya Patel",
    description: "Learn container orchestration patterns for high-traffic production environments.",
    capacity: 35,
    enrolled: 28,
    time: "1:00 PM - 4:00 PM",
    track: "devops",
  },
  {
    id: "ws-4",
    title: "AI-Powered Applications",
    speaker: "James Okonkwo",
    description: "Build intelligent applications using the Vercel AI SDK and modern LLM integration patterns.",
    capacity: 60,
    enrolled: 52,
    time: "1:00 PM - 4:00 PM",
    track: "ai",
  },
  {
    id: "ws-5",
    title: "GraphQL & Edge Computing",
    speaker: "Elena Voss",
    description: "Design performant GraphQL APIs deployed at the edge for global low-latency access.",
    capacity: 45,
    enrolled: 30,
    time: "9:00 AM - 12:00 PM",
    track: "backend",
  },
  {
    id: "ws-6",
    title: "Rust for Web Developers",
    speaker: "Kai Tanaka",
    description: "Introduction to Rust and WebAssembly for performance-critical web applications.",
    capacity: 30,
    enrolled: 25,
    time: "1:00 PM - 4:00 PM",
    track: "backend",
  },
];

// =============================================
// Dashboard mock data
// =============================================

export type DashboardStats = {
  totalRegistrations: number;
  revenue: number;
  workshopFillRate: number;
  averageTicketPrice: number;
};

export type RegistrationByDay = {
  date: string;
  registrations: number;
  revenue: number;
};

export type TicketDistribution = {
  tier: string;
  count: number;
  fill: string;
};

export const MOCK_STATS: DashboardStats = {
  totalRegistrations: 847,
  revenue: 412653,
  workshopFillRate: 78,
  averageTicketPrice: 487,
};

export const MOCK_REGISTRATIONS_BY_DAY: RegistrationByDay[] = [
  { date: "Jan 15", registrations: 12, revenue: 5988 },
  { date: "Jan 22", registrations: 28, revenue: 14572 },
  { date: "Jan 29", registrations: 45, revenue: 22455 },
  { date: "Feb 5", registrations: 67, revenue: 34233 },
  { date: "Feb 12", registrations: 89, revenue: 44111 },
  { date: "Feb 19", registrations: 120, revenue: 61080 },
  { date: "Feb 26", registrations: 156, revenue: 78444 },
  { date: "Mar 5", registrations: 134, revenue: 66866 },
  { date: "Mar 12", registrations: 110, revenue: 54890 },
  { date: "Mar 19", registrations: 86, revenue: 41014 },
];

export const MOCK_TICKET_DISTRIBUTION: TicketDistribution[] = [
  { tier: "Standard", count: 423, fill: "hsl(var(--chart-1))" },
  { tier: "Premium", count: 298, fill: "hsl(var(--chart-2))" },
  { tier: "VIP", count: 126, fill: "hsl(var(--chart-4))" },
];

export const MOCK_WORKSHOP_POPULARITY: { name: string; enrolled: number; capacity: number }[] = [
  { name: "Next.js 16", enrolled: 38, capacity: 50 },
  { name: "TypeScript", enrolled: 35, capacity: 40 },
  { name: "Kubernetes", enrolled: 28, capacity: 35 },
  { name: "AI Apps", enrolled: 52, capacity: 60 },
  { name: "GraphQL", enrolled: 30, capacity: 45 },
  { name: "Rust/WASM", enrolled: 25, capacity: 30 },
];
