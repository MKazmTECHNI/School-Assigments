"use client";

import * as React from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import ReCAPTCHA from "react-google-recaptcha";
import {
  registrationSchema,
  type RegistrationFormValues,
} from "@/lib/schema";
import { isRegistrationComplete, formatPrice, TICKET_PRICES } from "@/lib/types";
import type { RegistrationData, TicketTier } from "@/lib/types";
import { Form } from "@/components/ui/form";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import { Stepper, StepperStep } from "@/components/ui/stepper";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
  DialogClose,
} from "@/components/ui/dialog";
import { PersonalInfoStep } from "./personal-info-step";
import { TicketSelectionStep } from "./ticket-selection-step";
import { WorkshopStep } from "./workshop-step";
import { ReviewStep } from "./review-step";
import { ArrowLeft, ArrowRight, Send, User, Ticket, BookOpen, CheckCircle2 } from "lucide-react";

const STEP_FIELDS: (keyof RegistrationFormValues)[][] = [
  ["firstName", "lastName", "email", "phone", "company", "jobTitle"],
  ["tier", "dietaryPreference", "accessibilityNeeds", "tShirtSize"],
  ["selectedWorkshops", "specialRequests"],
  [],
];

export function RegistrationForm() {
  const [currentStep, setCurrentStep] = React.useState<number>(1);
  const [captchaToken, setCaptchaToken] = React.useState<string | null>(null);
  const [showSuccess, setShowSuccess] = React.useState<boolean>(false);
  const recaptchaRef = React.useRef<ReCAPTCHA | null>(null);

  const form = useForm<RegistrationFormValues>({
    resolver: zodResolver(registrationSchema),
    defaultValues: {
      firstName: "",
      lastName: "",
      email: "",
      phone: "",
      company: "",
      jobTitle: "",
      tier: undefined,
      dietaryPreference: undefined,
      accessibilityNeeds: "",
      tShirtSize: undefined,
      selectedWorkshops: [],
      specialRequests: "",
    },
    mode: "onTouched",
  });

  const totalSteps = 4;
  const progressValue = (currentStep / totalSteps) * 100;

  const validateCurrentStep = async (): Promise<boolean> => {
    const fields = STEP_FIELDS[currentStep - 1];
    if (!fields || fields.length === 0) return true;

    const result = await form.trigger(fields);
    return result;
  };

  const handleNext = async () => {
    const isValid = await validateCurrentStep();
    if (isValid && currentStep < totalSteps) {
      setCurrentStep((prev) => prev + 1);
    }
  };

  const handlePrev = () => {
    if (currentStep > 1) {
      setCurrentStep((prev) => prev - 1);
    }
  };

  const onSubmit = (data: RegistrationFormValues) => {
    if (!captchaToken) return;

    // Type predicate usage
    const fullData: Partial<RegistrationData> = { ...data };
    if (isRegistrationComplete(fullData)) {
      setShowSuccess(true);
    }
  };

  const handleCaptchaChange = (token: string | null) => {
    setCaptchaToken(token);
  };

  const handleReset = () => {
    setShowSuccess(false);
    setCurrentStep(1);
    setCaptchaToken(null);
    recaptchaRef.current?.reset();
    form.reset();
  };

  return (
    <>
      <Card className="w-full max-w-3xl mx-auto shadow-lg border-border/50">
        <CardContent className="p-6 sm:p-8">
          {/* Progress bar */}
          <div className="mb-6">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-medium text-muted-foreground">
                Step {currentStep} of {totalSteps}
              </span>
              <span className="text-xs font-medium text-primary font-mono">
                {Math.round(progressValue)}%
              </span>
            </div>
            <Progress value={progressValue} className="h-2" />
          </div>

          {/* Stepper navigation */}
          <Stepper activeStep={currentStep} className="mb-8">
            <StepperStep step={1} label="Personal Info" icon={<User className="h-4 w-4" />} />
            <StepperStep step={2} label="Ticket" icon={<Ticket className="h-4 w-4" />} />
            <StepperStep step={3} label="Workshops" icon={<BookOpen className="h-4 w-4" />} />
            <StepperStep step={4} label="Review" icon={<CheckCircle2 className="h-4 w-4" />} />
          </Stepper>

          {/* Form */}
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)}>
              {currentStep === 1 && (
                <div className="animate-fade-in" key="step-1">
                  <PersonalInfoStep />
                </div>
              )}

              {currentStep === 2 && (
                <div className="animate-fade-in" key="step-2">
                  <TicketSelectionStep />
                </div>
              )}

              {currentStep === 3 && (
                <div className="animate-fade-in" key="step-3">
                  <WorkshopStep />
                </div>
              )}

              {currentStep === 4 && (
                <div className="animate-fade-in" key="step-4">
                  <ReviewStep
                    recaptchaRef={recaptchaRef}
                    onCaptchaChange={handleCaptchaChange}
                  />
                </div>
              )}

              {/* Navigation buttons */}
              <div className="flex items-center justify-between mt-8 pt-6 border-t">
                <Button
                  type="button"
                  variant="outline"
                  onClick={handlePrev}
                  disabled={currentStep === 1}
                  className="gap-2 transition-all hover:gap-3 active:scale-95"
                >
                  <ArrowLeft className="h-4 w-4" />
                  Back
                </Button>

                {currentStep < totalSteps ? (
                  <Button
                    type="button"
                    onClick={handleNext}
                    className="gap-2 transition-all hover:gap-3 active:scale-95"
                  >
                    Next
                    <ArrowRight className="h-4 w-4" />
                  </Button>
                ) : (
                  <Button
                    type="submit"
                    disabled={!captchaToken}
                    className="gap-2 transition-all hover:gap-3 active:scale-95 disabled:opacity-50"
                  >
                    <Send className="h-4 w-4" />
                    Submit Registration
                  </Button>
                )}
              </div>
            </form>
          </Form>
        </CardContent>
      </Card>

      {/* Success Dialog */}
      <Dialog open={showSuccess} onOpenChange={setShowSuccess}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <div className="flex justify-center mb-4">
              <div className="flex h-16 w-16 items-center justify-center rounded-full bg-accent/10 animate-scale-in">
                <CheckCircle2 className="h-8 w-8 text-accent" />
              </div>
            </div>
            <DialogTitle className="text-center text-xl">Registration Complete!</DialogTitle>
            <DialogDescription className="text-center">
              Thank you, {form.getValues("firstName")}! Your{" "}
              <span className="font-semibold text-primary capitalize">
                {form.getValues("tier")}
              </span>{" "}
              ticket for TechConf 2026 has been confirmed. A confirmation email will be sent
              to {form.getValues("email")}.
            </DialogDescription>
          </DialogHeader>

          <div className="flex items-center justify-center rounded-lg bg-muted p-4">
            <div className="text-center">
              <p className="text-sm text-muted-foreground">Total Amount</p>
              <p className="text-2xl font-bold text-foreground font-mono">
                {formatPrice(TICKET_PRICES[(form.getValues("tier") as TicketTier) ?? "standard"])}
              </p>
            </div>
          </div>

          <DialogFooter className="sm:justify-center">
            <DialogClose asChild>
              <Button variant="outline" onClick={handleReset}>
                Register Another Attendee
              </Button>
            </DialogClose>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}


