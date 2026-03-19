"use client";

import { useFormContext } from "react-hook-form";
import type { RegistrationFormValues } from "@/lib/schema";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { TICKET_PRICES, WORKSHOPS, formatPrice } from "@/lib/types";
import type { TicketTier } from "@/lib/types";
import ReCAPTCHA from "react-google-recaptcha";

// =============================================
// TypeScript: Defining types for refs
// =============================================

type RecaptchaRef = React.RefObject<ReCAPTCHA | null>;

interface ReviewStepProps {
  recaptchaRef: RecaptchaRef;
  onCaptchaChange: (token: string | null) => void;
}

export function ReviewStep({ recaptchaRef, onCaptchaChange }: ReviewStepProps) {
  const { getValues } = useFormContext<RegistrationFormValues>();
  const values = getValues();

  const selectedWorkshopNames = WORKSHOPS.filter((ws) =>
    values.selectedWorkshops?.includes(ws.id)
  ).map((ws) => ws.title);

  // =============================================
  // TypeScript: Defining types for state (readonly display data)
  // =============================================
  const displaySections: Readonly<
    {
      title: string;
      items: { label: string; value: string }[];
    }[]
  > = [
    {
      title: "Personal Information",
      items: [
        { label: "Name", value: `${values.firstName} ${values.lastName}` },
        { label: "Email", value: values.email },
        { label: "Phone", value: values.phone },
        { label: "Company", value: values.company },
        { label: "Job Title", value: values.jobTitle },
      ],
    },
    {
      title: "Ticket Details",
      items: [
        {
          label: "Tier",
          value: `${(values.tier ?? "").charAt(0).toUpperCase() + (values.tier ?? "").slice(1)} - ${formatPrice(TICKET_PRICES[(values.tier as TicketTier) ?? "standard"])}`,
        },
        {
          label: "T-Shirt Size",
          value: (values.tShirtSize ?? "").toUpperCase(),
        },
        {
          label: "Dietary Preference",
          value:
            (values.dietaryPreference ?? "none") === "none"
              ? "No preference"
              : (values.dietaryPreference ?? "").charAt(0).toUpperCase() +
                (values.dietaryPreference ?? "").slice(1),
        },
        ...(values.accessibilityNeeds
          ? [{ label: "Accessibility", value: values.accessibilityNeeds }]
          : []),
      ],
    },
    {
      title: "Workshops",
      items: [
        {
          label: "Selected",
          value: selectedWorkshopNames.length > 0 ? selectedWorkshopNames.join(", ") : "None",
        },
        ...(values.specialRequests
          ? [{ label: "Special Requests", value: values.specialRequests }]
          : []),
      ],
    },
  ];

  return (
    <div className="animate-fade-in">
      <div className="mb-6">
        <h2 className="text-xl font-semibold text-foreground">Review Your Registration</h2>
        <p className="text-sm text-muted-foreground mt-1">
          Please verify all information below before submitting.
        </p>
      </div>

      <div className="flex flex-col gap-4">
        {displaySections.map((section, idx) => (
          <Card key={section.title} className="animate-scale-in" style={{ animationDelay: `${idx * 100}ms` }}>
            <CardHeader className="pb-3">
              <CardTitle className="text-base">{section.title}</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex flex-col gap-2">
                {section.items.map((item, itemIdx) => (
                  <div key={item.label}>
                    <div className="flex items-center justify-between py-1">
                      <span className="text-sm text-muted-foreground">{item.label}</span>
                      <span className="text-sm font-medium text-foreground text-right max-w-[60%]">
                        {item.value}
                      </span>
                    </div>
                    {itemIdx < section.items.length - 1 && <Separator />}
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        ))}

        <div className="flex justify-center mt-2">
          <ReCAPTCHA
            ref={recaptchaRef}
            sitekey="6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI"
            onChange={onCaptchaChange}
            theme="light"
          />
        </div>
        <p className="text-xs text-center text-muted-foreground">
          Please complete the CAPTCHA verification to submit your registration.
        </p>
      </div>
    </div>
  );
}
