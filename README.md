# Drawing Bezier Curve in C

Boot up .exe, click in 3 places, and program draws Bezier Curve. Simple

---

## Code breakdown

It's more as `<windows.h>` documentation, so I won't dwell on it much.

```c
#define WIDTH 777
#define HEIGHT 777
#define BEZIER_SMOOTHNESS 100.0
```

/\ Window sizes, and how much lines is the curve drawn with

```c
LRESULT CALLBACK WndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {
```

/\ Nagłówek Window Procedure, which operates on all events (msg) of the window, eg. clicks or closing, etc.

```c
case WM_PAINT: {
    PAINTSTRUCT ps;
    HDC hdc = BeginPaint(hwnd, &ps);

    for (int i = 0; i < click_count; ++i) {
        Ellipse(hdc, points[i].x - 3, points[i].y - 3, points[i].x + 4, points[i].y + 4);
    }
```

/\ Draws small dots in clicked places, to mark control points to Bezier Curve

```c
        if (bezier_drawn) {
            HPEN hPen = CreatePen(PS_SOLID, 2, RGB(0,0,255));
            HPEN hOldPen = (HPEN)SelectObject(hdc, hPen);
            POINT prev = points[0];
```

/\ When 3 points are chosen, it creates new pen. `PS_Solid` oznacza linię bez przecięć, `2` to grubość, a `RGB` to na logike kolor.

```c
            for (int i = 1; i <= BEZIER_SMOOTHNESS; ++i) {
                double t = i / BEZIER_SMOOTHNESS;

```

/\ Loop executes `BEZIER_SMOOTHNESS` times, drawing fragment by fragment of Bezier Curve. `t` changes from 0.01 to 1.0 and stands for % already drawn.

```c
                double x = (1-t)*(1-t)*points[0].x + 2*(1-t)*t*points[1].x + t*t*points[2].x;
                double y = (1-t)*(1-t)*points[0].y + 2*(1-t)*t*points[1].y + t*t*points[2].y;

```

/\ Quadratic formula to calculate x i y of next point (`1-t` gives value from ~1 to 0, (% sunfinished))

Equations for x and y:
$$ x = (1-t)^2 x_0 + 2(1-t)t x_1 + t^2 x_2 $$
$$ y = (1-t)^2 y_0 + 2(1-t)t y_1 + t^2 y_2 $$

---

```c
                MoveToEx(hdc, prev.x, prev.y, NULL);
                LineTo(hdc, (int)x, (int)y);
                prev.x = (int)x;
                prev.y = (int)y;
            }
```

/\ Moves to `prev` and draws line to calculated point, then updates prev.

## Fun fact

if you set smoothness high enough, it'll look like animation. I suppose you could also just add sleep to loop, but wheres the fun in that.
