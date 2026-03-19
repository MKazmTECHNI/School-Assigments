"use client";

import { useFormContext } from "react-hook-form";
import type { RegistrationFormValues } from "@/lib/schema";
import {
  FormField,
  FormItem,
  FormLabel,
  FormControl,
  FormMessage,
  FormDescription,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Card, CardContent } from "@/components/ui/card";
import { cn } from "@/lib/utils";
import { TICKET_PRICES, formatPrice } from "@/lib/types";
import type { TicketTier } from "@/lib/types";
import { Tooltip, TooltipTrigger, TooltipContent } from "@/components/ui/tooltip";
import { HelpCircle, Crown, Star, Ticket } from "lucide-react";

const ticketOptions: {
  value: TicketTier;
  label: string;
  description: string;
  features: string[];
  icon: React.ReactNode;
}[] = [
  {
    value: "standard",
    label: "Standard",
    description: "Essential conference access",
    features: ["All keynotes", "Lunch included", "1 workshop"],
    icon: <Ticket className="h-5 w-5" />,
  },
  {
    value: "premium",
    label: "Premium",
    description: "Enhanced experience",
    features: ["All keynotes", "Lunch & dinner", "2 workshops", "Networking lounge"],
    icon: <Star className="h-5 w-5" />,
  },
  {
    value: "vip",
    label: "VIP",
    description: "Ultimate conference experience",
    features: [
      "All keynotes",
      "All meals",
      "3 workshops",
      "VIP lounge",
      "Speaker meet & greet",
    ],
    icon: <Crown className="h-5 w-5" />,
  },
];

export function TicketSelectionStep() {
  const { control, watch } = useFormContext<RegistrationFormValues>();
  const selectedTier = watch("tier");

  return (
    <div className="animate-fade-in">
      <div className="mb-6">
        <h2 className="text-xl font-semibold text-foreground">Select Your Ticket</h2>
        <p className="text-sm text-muted-foreground mt-1">
          Choose the ticket tier that best suits your conference needs.
        </p>
      </div>

      <FormField
        control={control}
        name="tier"
        render={({ field }) => (
          <FormItem className="mb-6">
            <FormLabel className="sr-only">Ticket Tier</FormLabel>
            <FormControl>
              {/* Container query: cards adapt based on parent width */}
              <div className="@container">
                <div className="grid grid-cols-1 gap-3 @md:grid-cols-3">
                  {ticketOptions.map((option) => (
                    <Card
                      key={option.value}
                      className={cn(
                        "cursor-pointer transition-all duration-300 group",
                        "hover:shadow-md hover:-translate-y-0.5",
                        "active:scale-[0.98]",
                        field.value === option.value
                          ? "border-primary ring-2 ring-primary/20 shadow-lg"
                          : "border-border hover:border-primary/40"
                      )}
                      onClick={() => field.onChange(option.value)}
                      role="radio"
                      aria-checked={field.value === option.value}
                      tabIndex={0}
                      onKeyDown={(e) => {
                        if (e.key === "Enter" || e.key === " ") {
                          e.preventDefault();
                          field.onChange(option.value);
                        }
                      }}
                    >
                      <CardContent className="p-4">
                        <div className="flex items-start justify-between mb-3">
                          <div
                            className={cn(
                              "flex h-10 w-10 items-center justify-center rounded-lg transition-colors",
                              field.value === option.value
                                ? "bg-primary text-primary-foreground"
                                : "bg-muted text-muted-foreground group-hover:bg-primary/10 group-hover:text-primary"
                            )}
                          >
                            {option.icon}
                          </div>
                          <span className="text-lg font-bold text-foreground font-mono">
                            {formatPrice(TICKET_PRICES[option.value])}
                          </span>
                        </div>
                        <h3 className="font-semibold text-foreground">{option.label}</h3>
                        <p className="text-xs text-muted-foreground mt-1 mb-3">
                          {option.description}
                        </p>
                        <ul className="text-xs text-muted-foreground flex flex-col gap-1.5">
                          {option.features.map((feature) => (
                            <li key={feature} className="flex items-center gap-1.5">
                              <div className="h-1.5 w-1.5 rounded-full bg-accent shrink-0" />
                              {feature}
                            </li>
                          ))}
                        </ul>
                      </CardContent>
                    </Card>
                  ))}
                </div>
              </div>
            </FormControl>
            <FormMessage />
          </FormItem>
        )}
      />

      {selectedTier && (
        <div className="animate-slide-in-right flex flex-col gap-4 rounded-lg border bg-muted/50 p-4">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <FormField
              control={control}
              name="tShirtSize"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>T-Shirt Size</FormLabel>
                  <Select onValueChange={field.onChange} defaultValue={field.value}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder="Select size" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      <SelectItem value="xs">XS</SelectItem>
                      <SelectItem value="s">S</SelectItem>
                      <SelectItem value="m">M</SelectItem>
                      <SelectItem value="l">L</SelectItem>
                      <SelectItem value="xl">XL</SelectItem>
                      <SelectItem value="xxl">XXL</SelectItem>
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={control}
              name="dietaryPreference"
              render={({ field }) => (
                <FormItem>
                  <div className="flex items-center gap-1">
                    <FormLabel>Dietary Preference</FormLabel>
                    <Tooltip>
                      <TooltipTrigger type="button" tabIndex={-1}>
                        <HelpCircle className="h-3.5 w-3.5 text-muted-foreground" />
                      </TooltipTrigger>
                      <TooltipContent>
                        <p>We accommodate all dietary needs at our catered events.</p>
                      </TooltipContent>
                    </Tooltip>
                  </div>
                  <Select onValueChange={field.onChange} defaultValue={field.value}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder="Select preference" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      <SelectItem value="none">No preference</SelectItem>
                      <SelectItem value="vegetarian">Vegetarian</SelectItem>
                      <SelectItem value="vegan">Vegan</SelectItem>
                      <SelectItem value="gluten-free">Gluten-Free</SelectItem>
                      <SelectItem value="halal">Halal</SelectItem>
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />
          </div>

          <FormField
            control={control}
            name="accessibilityNeeds"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Accessibility Needs</FormLabel>
                <FormControl>
                  <Input
                    placeholder="Any special accessibility requirements..."
                    {...field}
                  />
                </FormControl>
                <FormDescription>
                  Optional. Let us know if you have any accessibility requirements.
                </FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />
        </div>
      )}
    </div>
  );
}
