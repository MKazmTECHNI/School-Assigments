"use client";

import * as React from "react";
import { Tooltip as RechartsTooltipBase } from "recharts";
import { cn } from "@/lib/utils";

// =============================================
// Chart config and context
// =============================================

export type ChartConfig = Record<
  string,
  {
    label?: React.ReactNode;
    icon?: React.ComponentType;
    color?: string;
    theme?: Record<string, string>;
  }
>;

type ChartContextProps = {
  config: ChartConfig;
};

const ChartContext = React.createContext<ChartContextProps | null>(null);

function useChart() {
  const context = React.useContext(ChartContext);
  if (!context) {
    throw new Error("useChart must be used within a <ChartContainer />");
  }
  return context;
}

// =============================================
// ChartContainer
// =============================================

const ChartContainer = React.forwardRef<
  HTMLDivElement,
  React.ComponentProps<"div"> & {
    config: ChartConfig;
    children: React.ComponentProps<"div">["children"];
  }
>(({ id, className, children, config, ...props }, ref) => {
  const uniqueId = React.useId();
  const chartId = `chart-${id || uniqueId.replace(/:/g, "")}`;

  return (
    <ChartContext.Provider value={{ config }}>
      <div
        data-chart={chartId}
        ref={ref}
        className={cn(
          "flex aspect-video justify-center text-xs [&_.recharts-cartesian-axis-tick_text]:fill-muted-foreground [&_.recharts-cartesian-grid_line[stroke='#ccc']]:stroke-border/50 [&_.recharts-curve.recharts-tooltip-cursor]:stroke-border [&_.recharts-dot[stroke='#fff']]:stroke-transparent [&_.recharts-layer]:outline-none [&_.recharts-polar-grid_[stroke='#ccc']]:stroke-border [&_.recharts-radial-bar-background-sector]:fill-muted [&_.recharts-rectangle.recharts-tooltip-cursor]:fill-muted [&_.recharts-reference-line_[stroke='#ccc']]:stroke-border [&_.recharts-sector[stroke='#fff']]:stroke-transparent [&_.recharts-sector]:outline-none [&_.recharts-surface]:outline-none",
          className
        )}
        {...props}
      >
        <style
          dangerouslySetInnerHTML={{
            __html: Object.entries(config)
              .map(([key, value]) => {
                const color = value.color;
                return color
                  ? `[data-chart="${chartId}"] { --color-${key}: ${color}; }`
                  : "";
              })
              .join("\n"),
          }}
        />
        {children}
      </div>
    </ChartContext.Provider>
  );
});
ChartContainer.displayName = "ChartContainer";

// =============================================
// ChartTooltip and ChartTooltipContent
// =============================================

const ChartTooltip = RechartsTooltipBase;

interface ChartTooltipContentProps {
  active?: boolean;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  payload?: any[];
  label?: string;
  hideLabel?: boolean;
  indicator?: "line" | "dot" | "dashed";
  className?: string;
}

function ChartTooltipContent({
  active,
  payload,
  label,
  hideLabel = false,
  indicator = "dot",
  className,
}: ChartTooltipContentProps) {
  const { config } = useChart();

  if (!active || !payload?.length) return null;

  return (
    <div
      className={cn(
        "grid min-w-[8rem] items-start gap-1.5 rounded-lg border border-border/50 bg-background px-2.5 py-1.5 text-xs shadow-xl",
        className
      )}
    >
      {!hideLabel && label && (
        <div className="font-medium text-foreground">{label}</div>
      )}
      <div className="grid gap-1.5">
        {payload.map((item: { dataKey?: string; name?: string; value?: number; color?: string }, index: number) => {
          const key = String(item.dataKey || item.name || index);
          const itemConfig = config[key] || {};
          const indicatorColor = item.color || itemConfig.color || "hsl(var(--chart-1))";

          return (
            <div key={key} className="flex items-center gap-2">
              {indicator === "dot" && (
                <div
                  className="h-2.5 w-2.5 shrink-0 rounded-[2px]"
                  style={{ backgroundColor: indicatorColor }}
                />
              )}
              {indicator === "line" && (
                <div
                  className="h-0.5 w-4 shrink-0"
                  style={{ backgroundColor: indicatorColor }}
                />
              )}
              <div className="flex flex-1 items-center justify-between gap-4">
                <span className="text-muted-foreground">
                  {String(itemConfig.label || item.name || key)}
                </span>
                <span className="font-mono font-medium tabular-nums text-foreground">
                  {typeof item.value === "number" ? item.value.toLocaleString() : item.value}
                </span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
ChartTooltipContent.displayName = "ChartTooltipContent";

export {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  useChart,
};
