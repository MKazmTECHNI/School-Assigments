import { z } from "zod";

// =============================================
// Zod: Regex patterns for validation
// =============================================

const PHONE_REGEX = /^\+?[1-9]\d{1,14}$/;
const NAME_REGEX = /^[A-Za-zÀ-ÖØ-öø-ÿ'\-\s]{2,50}$/;

// =============================================
// Step 1: Personal Info Schema
// =============================================

export const personalInfoSchema = z.object({
  firstName: z
    .string()
    .min(2, "First name must be at least 2 characters")
    .max(50, "First name must be at most 50 characters")
    .regex(NAME_REGEX, "First name can only contain letters, hyphens, and apostrophes"),
  lastName: z
    .string()
    .min(2, "Last name must be at least 2 characters")
    .max(50, "Last name must be at most 50 characters")
    .regex(NAME_REGEX, "Last name can only contain letters, hyphens, and apostrophes"),
  email: z
    .string()
    .email("Please enter a valid email address")
    .refine(
      (email) => !email.endsWith("@example.com"),
      "Please use a real email address, not @example.com"
    ),
  phone: z
    .string()
    .regex(PHONE_REGEX, "Please enter a valid phone number (e.g., +1234567890)"),
  company: z
    .string()
    .min(1, "Company name is required")
    .max(100, "Company name must be at most 100 characters"),
  jobTitle: z
    .string()
    .min(1, "Job title is required")
    .max(100, "Job title must be at most 100 characters"),
});

// =============================================
// Step 2: Ticket Selection Schema (with refine)
// =============================================

export const ticketSchema = z.object({
  tier: z.enum(["standard", "premium", "vip"], {
    required_error: "Please select a ticket tier",
  }),
  dietaryPreference: z.enum(["none", "vegetarian", "vegan", "gluten-free", "halal"], {
    required_error: "Please select a dietary preference",
  }),
  accessibilityNeeds: z.string().max(500, "Please keep accessibility notes under 500 characters"),
  tShirtSize: z.enum(["xs", "s", "m", "l", "xl", "xxl"], {
    required_error: "Please select a t-shirt size",
  }),
});

// =============================================
// Step 3: Workshop Preferences Schema (with refine)
// =============================================

export const workshopSchema = z
  .object({
    selectedWorkshops: z
      .array(z.string())
      .min(1, "Please select at least one workshop")
      .max(3, "You can select up to 3 workshops"),
    specialRequests: z
      .string()
      .max(500, "Please keep special requests under 500 characters"),
  })
  .refine(
    (data) => {
      const uniqueWorkshops = new Set(data.selectedWorkshops);
      return uniqueWorkshops.size === data.selectedWorkshops.length;
    },
    {
      message: "You cannot select the same workshop twice",
      path: ["selectedWorkshops"],
    }
  );

// =============================================
// Combined registration schema
// =============================================

export const registrationSchema = personalInfoSchema
  .merge(ticketSchema)
  .merge(
    z.object({
      selectedWorkshops: z
        .array(z.string())
        .min(1, "Please select at least one workshop")
        .max(3, "You can select up to 3 workshops"),
      specialRequests: z
        .string()
        .max(500, "Please keep special requests under 500 characters"),
    })
  );

export type RegistrationFormValues = z.infer<typeof registrationSchema>;
