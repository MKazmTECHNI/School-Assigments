import type { Metadata } from "next";
import { SiteHeader } from "@/components/site-header";
import { RegistrationForm } from "@/components/registration/registration-form";

export const metadata: Metadata = {
  title: "Register - TechConf 2026",
  description:
    "Register for TechConf 2026. Choose your ticket, select workshops, and secure your spot at the premier developer conference.",
};

export default function RegisterPage() {
  return (
    <div className="min-h-screen bg-background">
      <SiteHeader />
      <main className="container mx-auto px-4 py-8 sm:py-12">
        <div className="text-center mb-8 animate-fade-in">
          <h1 className="text-3xl font-bold text-foreground sm:text-4xl text-balance">
            Register for TechConf 2026
          </h1>
          <p className="mt-2 text-muted-foreground max-w-lg mx-auto text-pretty">
            Complete the form below to secure your spot at the premier developer
            conference of the year.
          </p>
        </div>
        <RegistrationForm />
      </main>
    </div>
  );
}
