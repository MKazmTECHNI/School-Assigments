"use client";

import * as React from "react";
import { cn } from "@/lib/utils";

// =============================================
// Compound Components pattern: Stepper
// =============================================

type StepperContextValue = {
  activeStep: number;
  totalSteps: number;
};

const StepperContext = React.createContext<StepperContextValue | null>(null);

function useStepperContext(): StepperContextValue {
  const context = React.useContext(StepperContext);
  if (!context) {
    throw new Error("Stepper compound components must be used within <Stepper>");
  }
  return context;
}

// ---- Stepper Root ----

interface StepperProps extends React.HTMLAttributes<HTMLDivElement> {
  activeStep: number;
  children: React.ReactNode;
}

function Stepper({ activeStep, children, className, ...props }: StepperProps) {
  const totalSteps = React.Children.toArray(children).filter(
    (child) => React.isValidElement(child) && child.type === StepperStep
  ).length;

  return (
    <StepperContext.Provider value={{ activeStep, totalSteps }}>
      <nav
        aria-label="Registration progress"
        className={cn("flex items-center justify-between", className)}
        {...props}
      >
        {children}
      </nav>
    </StepperContext.Provider>
  );
}

// ---- StepperStep ----

interface StepperStepProps extends React.HTMLAttributes<HTMLDivElement> {
  step: number;
  label: string;
  icon?: React.ReactNode;
}

function StepperStep({ step, label, icon, className, ...props }: StepperStepProps) {
  const { activeStep, totalSteps } = useStepperContext();
  const isActive = step === activeStep;
  const isCompleted = step < activeStep;
  const isLast = step === totalSteps;

  return (
    <div className={cn("flex items-center", !isLast && "flex-1", className)} {...props}>
      <div className="flex flex-col items-center gap-1">
        <div
          className={cn(
            "flex h-10 w-10 items-center justify-center rounded-full border-2 text-sm font-semibold transition-all duration-300",
            isActive &&
              "border-primary bg-primary text-primary-foreground scale-110 shadow-lg shadow-primary/25",
            isCompleted && "border-accent bg-accent text-accent-foreground",
            !isActive && !isCompleted && "border-muted-foreground/30 text-muted-foreground"
          )}
        >
          {isCompleted ? (
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="3"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <polyline points="20 6 9 17 4 12" />
            </svg>
          ) : (
            icon ?? step
          )}
        </div>
        <span
          className={cn(
            "text-xs font-medium transition-colors hidden sm:block",
            isActive && "text-primary",
            isCompleted && "text-accent",
            !isActive && !isCompleted && "text-muted-foreground"
          )}
        >
          {label}
        </span>
      </div>
      {!isLast && (
        <div className="mx-2 h-[2px] flex-1">
          <div
            className={cn(
              "h-full rounded-full transition-all duration-500",
              isCompleted ? "bg-accent" : "bg-border"
            )}
          />
        </div>
      )}
    </div>
  );
}

// ---- StepperContent ----

interface StepperContentProps extends React.HTMLAttributes<HTMLDivElement> {
  step: number;
  children: React.ReactNode;
}

function StepperContent({ step, children, className, ...props }: StepperContentProps) {
  const { activeStep } = useStepperContext();

  if (step !== activeStep) return null;

  return (
    <div
      className={cn("animate-fade-in", className)}
      role="tabpanel"
      aria-label={`Step ${step}`}
      {...props}
    >
      {children}
    </div>
  );
}

// Attach sub-components for Compound Components pattern
const StepperCompound = Object.assign(Stepper, {
  Step: StepperStep,
  Content: StepperContent,
});

export { StepperCompound as Stepper, StepperStep, StepperContent, useStepperContext };
