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
import { Checkbox } from "@/components/ui/checkbox";
import { Card, CardContent } from "@/components/ui/card";
import { cn } from "@/lib/utils";
import { WORKSHOPS } from "@/lib/types";
import type { Workshop } from "@/lib/types";
import { Tooltip, TooltipTrigger, TooltipContent } from "@/components/ui/tooltip";
import { Clock, Users, HelpCircle } from "lucide-react";

// =============================================
// TypeScript: Generic component
// =============================================

type WorkshopCardProps<T extends Workshop> = {
  workshop: T;
  isSelected: boolean;
  isDisabled: boolean;
  onToggle: (id: string) => void;
};

function WorkshopCard<T extends Workshop>({
  workshop,
  isSelected,
  isDisabled,
  onToggle,
}: WorkshopCardProps<T>) {
  const fillPercent = Math.round((workshop.enrolled / workshop.capacity) * 100);
  const spotsLeft = workshop.capacity - workshop.enrolled;

  const trackColors: Record<string, string> = {
    frontend: "bg-primary/10 text-primary",
    backend: "bg-accent/10 text-accent",
    devops: "bg-muted text-muted-foreground",
    ai: "bg-destructive/10 text-destructive",
  };

  return (
    <Card
      className={cn(
        "transition-all duration-200 group",
        isSelected && "border-primary ring-2 ring-primary/20",
        isDisabled && !isSelected && "opacity-50"
      )}
    >
      <CardContent className="p-4">
        <div className="flex items-start justify-between mb-2">
          <span
            className={cn(
              "inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium",
              trackColors[workshop.track] ?? "bg-muted text-muted-foreground"
            )}
          >
            {workshop.track.charAt(0).toUpperCase() + workshop.track.slice(1)}
          </span>
          <button
            type="button"
            disabled={isDisabled && !isSelected}
            className="flex items-center cursor-pointer"
            aria-label={`Select ${workshop.title}`}
          >
            <Checkbox
              checked={isSelected}
              onCheckedChange={() => {
                if (!isDisabled || isSelected) {
                  onToggle(workshop.id);
                }
              }}
              disabled={isDisabled && !isSelected}
            />
          </button>
        </div>

        <h3 className="font-semibold text-foreground text-sm mb-1">{workshop.title}</h3>
        <p className="text-xs text-muted-foreground mb-1">by {workshop.speaker}</p>
        <p className="text-xs text-muted-foreground mb-3 leading-relaxed line-clamp-2">
          {workshop.description}
        </p>

        <div className="flex items-center justify-between text-xs text-muted-foreground">
          <div className="flex items-center gap-1">
            <Clock className="h-3 w-3" />
            <span>{workshop.time}</span>
          </div>
          <Tooltip>
            <TooltipTrigger asChild>
              <div className="flex items-center gap-1">
                <Users className="h-3 w-3" />
                <span>{spotsLeft} spots left</span>
              </div>
            </TooltipTrigger>
            <TooltipContent>
              <p>{fillPercent}% full ({workshop.enrolled}/{workshop.capacity})</p>
            </TooltipContent>
          </Tooltip>
        </div>

        <div className="mt-2 h-1.5 w-full overflow-hidden rounded-full bg-secondary">
          <div
            className={cn(
              "h-full rounded-full transition-all",
              fillPercent > 80 ? "bg-destructive" : fillPercent > 50 ? "bg-primary" : "bg-accent"
            )}
            style={{ width: `${fillPercent}%` }}
          />
        </div>
      </CardContent>
    </Card>
  );
}

export function WorkshopStep() {
  const { control, watch } = useFormContext<RegistrationFormValues>();
  const selectedTier = watch("tier");
  const maxWorkshops = selectedTier === "vip" ? 3 : selectedTier === "premium" ? 2 : 1;

  return (
    <div className="animate-fade-in">
      <div className="mb-6">
        <div className="flex items-center gap-2">
          <h2 className="text-xl font-semibold text-foreground">Workshop Preferences</h2>
          <Tooltip>
            <TooltipTrigger type="button" tabIndex={-1}>
              <HelpCircle className="h-4 w-4 text-muted-foreground" />
            </TooltipTrigger>
            <TooltipContent>
              <p>Your {selectedTier} ticket includes up to {maxWorkshops} workshop{maxWorkshops > 1 ? "s" : ""}.</p>
            </TooltipContent>
          </Tooltip>
        </div>
        <p className="text-sm text-muted-foreground mt-1">
          Select up to {maxWorkshops} workshop{maxWorkshops > 1 ? "s" : ""} based on your{" "}
          <span className="font-medium text-primary capitalize">{selectedTier}</span> ticket.
        </p>
      </div>

      <FormField
        control={control}
        name="selectedWorkshops"
        render={({ field }) => {
          const currentSelections: string[] = field.value ?? [];

          const toggleWorkshop = (id: string) => {
            if (currentSelections.includes(id)) {
              field.onChange(currentSelections.filter((w: string) => w !== id));
            } else if (currentSelections.length < maxWorkshops) {
              field.onChange([...currentSelections, id]);
            }
          };

          return (
            <FormItem>
              <FormLabel className="sr-only">Workshops</FormLabel>
              <FormControl>
                {/* Container query on workshop grid */}
                <div className="@container">
                  <div className="grid grid-cols-1 gap-3 @sm:grid-cols-2 @lg:grid-cols-3">
                    {WORKSHOPS.map((ws) => (
                      <WorkshopCard
                        key={ws.id}
                        workshop={ws}
                        isSelected={currentSelections.includes(ws.id)}
                        isDisabled={currentSelections.length >= maxWorkshops}
                        onToggle={toggleWorkshop}
                      />
                    ))}
                  </div>
                </div>
              </FormControl>
              <FormMessage />
            </FormItem>
          );
        }}
      />

      <FormField
        control={control}
        name="specialRequests"
        render={({ field }) => (
          <FormItem className="mt-4">
            <FormLabel>Special Requests</FormLabel>
            <FormControl>
              <Input
                placeholder="Any specific topics or tools you'd like covered?"
                {...field}
              />
            </FormControl>
            <FormDescription>
              Optional. Let workshop instructors know if you have any preferences.
            </FormDescription>
            <FormMessage />
          </FormItem>
        )}
      />
    </div>
  );
}
