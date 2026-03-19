import type { Metadata } from "next";
import { SiteHeader } from "@/components/site-header";
import { DashboardContent } from "@/components/dashboard/dashboard-content";

export const metadata: Metadata = {
  title: "Dashboard - TechConf 2026",
  description:
    "View attendee statistics, registration trends, ticket distribution, and workshop popularity for TechConf 2026.",
};

export default function DashboardPage() {
  return (
    <div className="min-h-screen bg-background">
      <SiteHeader />
      <main className="container mx-auto px-4 py-8 sm:py-12">
        <div className="mb-8 animate-fade-in">
          <h1 className="text-3xl font-bold text-foreground sm:text-4xl text-balance">
            Conference Dashboard
          </h1>
          <p className="mt-2 text-muted-foreground text-pretty">
            Real-time statistics and analytics for TechConf 2026 registrations.
          </p>
        </div>
        <DashboardContent />
      </main>
    </div>
  );
}
