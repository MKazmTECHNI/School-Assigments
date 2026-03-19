import Link from "next/link";
import { SiteHeader } from "@/components/site-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import {
  ArrowRight,
  Calendar,
  MapPin,
  Users,
  Code2,
  Cpu,
  Globe,
  Sparkles,
} from "lucide-react";

const highlights = [
  {
    icon: Code2,
    title: "50+ Sessions",
    description: "Deep-dive talks on frontend, backend, DevOps, and AI.",
  },
  {
    icon: Cpu,
    title: "Hands-on Workshops",
    description: "Practical workshops led by industry experts and OSS maintainers.",
  },
  {
    icon: Globe,
    title: "Global Speakers",
    description: "Speakers from top companies worldwide sharing cutting-edge insights.",
  },
  {
    icon: Users,
    title: "Networking",
    description: "Connect with 1,000+ developers, architects, and tech leaders.",
  },
];

export default function HomePage() {
  return (
    <div className="min-h-screen bg-background">
      <SiteHeader />

      <main>
        {/* Hero section */}
        <section className="relative overflow-hidden">
          <div className="absolute inset-0 bg-gradient-to-b from-primary/5 via-transparent to-transparent" />
          <div className="container mx-auto px-4 py-16 sm:py-24 relative">
            <div className="flex flex-col items-center text-center animate-fade-in">
              <div className="inline-flex items-center gap-2 rounded-full border bg-muted px-4 py-1.5 text-sm text-muted-foreground mb-6">
                <Calendar className="h-3.5 w-3.5" />
                <span>June 15-17, 2026</span>
                <Separator orientation="vertical" className="h-4" />
                <MapPin className="h-3.5 w-3.5" />
                <span>San Francisco, CA</span>
              </div>

              <h1 className="text-4xl font-bold tracking-tight text-foreground sm:text-6xl lg:text-7xl text-balance max-w-4xl">
                The Premier{" "}
                <span className="text-primary">Developer Conference</span>{" "}
                of 2026
              </h1>

              <p className="mt-6 max-w-2xl text-lg text-muted-foreground leading-relaxed text-pretty">
                Three days of world-class talks, workshops, and networking.
                Join 1,000+ developers, engineers, and tech leaders at
                TechConf 2026.
              </p>

              <div className="mt-8 flex flex-col gap-3 sm:flex-row">
                <Button asChild size="lg" className="gap-2 group">
                  <Link href="/register">
                    Register Now
                    <ArrowRight className="h-4 w-4 transition-transform group-hover:translate-x-1" />
                  </Link>
                </Button>
                <Button asChild variant="outline" size="lg">
                  <Link href="/dashboard">View Statistics</Link>
                </Button>
              </div>

              {/* Animated stats */}
              <div className="mt-16 grid grid-cols-2 gap-6 sm:grid-cols-4 w-full max-w-2xl">
                {[
                  { label: "Attendees", value: "1,000+" },
                  { label: "Speakers", value: "50+" },
                  { label: "Workshops", value: "12" },
                  { label: "Days", value: "3" },
                ].map((stat, i) => (
                  <div
                    key={stat.label}
                    className="animate-scale-in flex flex-col items-center"
                    style={{ animationDelay: `${i * 100}ms` }}
                  >
                    <span className="text-2xl font-bold text-primary font-mono sm:text-3xl">
                      {stat.value}
                    </span>
                    <span className="text-sm text-muted-foreground">{stat.label}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </section>

        {/* Highlights section */}
        <section className="container mx-auto px-4 py-16">
          <div className="text-center mb-12">
            <h2 className="text-2xl font-bold text-foreground sm:text-3xl text-balance">
              Why Attend TechConf 2026?
            </h2>
            <p className="mt-2 text-muted-foreground max-w-lg mx-auto text-pretty">
              Everything you need to level up your skills and expand your network.
            </p>
          </div>

          {/* Container query grid */}
          <div className="@container">
            <div className="grid grid-cols-1 gap-4 @sm:grid-cols-2 @lg:grid-cols-4">
              {highlights.map((item, i) => {
                const Icon = item.icon;
                return (
                  <Card
                    key={item.title}
                    className="group transition-all duration-300 hover:shadow-lg hover:-translate-y-1 animate-fade-in"
                    style={{ animationDelay: `${i * 150}ms` }}
                  >
                    <CardContent className="p-6">
                      <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary/10 text-primary transition-colors group-hover:bg-primary group-hover:text-primary-foreground mb-4">
                        <Icon className="h-6 w-6" />
                      </div>
                      <h3 className="font-semibold text-foreground mb-1">{item.title}</h3>
                      <p className="text-sm text-muted-foreground leading-relaxed">
                        {item.description}
                      </p>
                    </CardContent>
                  </Card>
                );
              })}
            </div>
          </div>
        </section>

        {/* CTA section */}
        <section className="container mx-auto px-4 py-16">
          <Card className="overflow-hidden">
            <CardContent className="p-0">
              <div className="flex flex-col items-center text-center p-8 sm:p-12 bg-primary/5">
                <Sparkles className="h-8 w-8 text-primary mb-4" />
                <h2 className="text-2xl font-bold text-foreground sm:text-3xl text-balance">
                  Ready to Join Us?
                </h2>
                <p className="mt-3 text-muted-foreground max-w-md text-pretty">
                  Secure your spot today. Early bird tickets are limited and selling fast.
                </p>
                <Button asChild size="lg" className="mt-6 gap-2 group">
                  <Link href="/register">
                    Register Now
                    <ArrowRight className="h-4 w-4 transition-transform group-hover:translate-x-1" />
                  </Link>
                </Button>
              </div>
            </CardContent>
          </Card>
        </section>

        {/* Footer */}
        <footer className="border-t bg-muted/30">
          <div className="container mx-auto px-4 py-8 flex flex-col items-center gap-2 sm:flex-row sm:justify-between">
            <p className="text-sm text-muted-foreground">
              TechConf 2026. All rights reserved.
            </p>
            <p className="text-sm text-muted-foreground">
              San Francisco, CA &middot; June 15-17, 2026
            </p>
          </div>
        </footer>
      </main>
    </div>
  );
}
