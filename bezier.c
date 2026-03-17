#include <windows.h>
#include <math.h>

#define WIDTH 777
#define HEIGHT 777 // 7 is the lucky number
#define BEZIER_SMOOTHNESS 100.0

POINT points[3];
int click_count = 0;
BOOL bezier_drawn = FALSE;

LRESULT CALLBACK WndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {
    switch (msg) {
    case WM_LBUTTONDOWN:
        if (click_count < 3) {
            points[click_count].x = LOWORD(lParam);
            points[click_count].y = HIWORD(lParam);
            click_count++;
            if (click_count == 3) {
                bezier_drawn = TRUE;
            }
            InvalidateRect(hwnd, NULL, TRUE);
        }
        break;
    case WM_PAINT: {
        PAINTSTRUCT ps;
        HDC hdc = BeginPaint(hwnd, &ps);

        // Gdzie kliknięto, rysuj kropki
        for (int i = 0; i < click_count; ++i) {
            Ellipse(hdc, points[i].x - 3, points[i].y - 3, points[i].x + 4, points[i].y + 4);
        }

        if (bezier_drawn) { // Szczerze, nie mam zamiaru tego komentować, wierze że pan się nie będzie pytał
            HPEN hPen = CreatePen(PS_SOLID, 2, RGB(0,0,255));
            HPEN hOldPen = (HPEN)SelectObject(hdc, hPen);
            POINT prev = points[0];
            for (int i = 1; i <= BEZIER_SMOOTHNESS; ++i) {
                double t = i / BEZIER_SMOOTHNESS;
                double x = (1-t)*(1-t)*points[0].x + 2*(1-t)*t*points[1].x + t*t*points[2].x;
                double y = (1-t)*(1-t)*points[0].y + 2*(1-t)*t*points[1].y + t*t*points[2].y;
                MoveToEx(hdc, prev.x, prev.y, NULL);
                LineTo(hdc, (int)x, (int)y);
                prev.x = (int)x;
                prev.y = (int)y;
            }
            SelectObject(hdc, hOldPen);
            DeleteObject(hPen);
        }

        EndPaint(hwnd, &ps);
        break;
    }
    case WM_DESTROY:
        PostQuitMessage(0);
        break;
    default:
        return DefWindowProc(hwnd, msg, wParam, lParam);
    }
    return 0;
}

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow) {
    // Ustawienia okna
    WNDCLASS wc = {0};
    wc.lpfnWndProc = WndProc;
    wc.hInstance = hInstance;
    wc.lpszClassName = "BezierWin";
    wc.hbrBackground = (HBRUSH)(COLOR_WINDOW+1);

    RegisterClass(&wc);

    HWND hwnd = CreateWindow("BezierWin", "Krzywa Beziera", WS_OVERLAPPEDWINDOW,
        CW_USEDEFAULT, CW_USEDEFAULT, WIDTH, HEIGHT, NULL, NULL, hInstance, NULL);

    ShowWindow(hwnd, nCmdShow);

    MSG msg;
    while (GetMessage(&msg, NULL, 0, 0)) {
        TranslateMessage(&msg);
        DispatchMessage(&msg);
    }
    return 0;
}

// zestrzele się chyba o tej godzinie