package S.N.A.K.E;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private DrawingView drawingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        drawingView = new DrawingView(this);
        setContentView(drawingView);

        drawingView.setBackgroundColor(Color.BLACK);

        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (controller != null) {
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            controller.hide(WindowInsetsCompat.Type.systemBars());
        }
    }

    private class DrawingView extends View {
        private Map<Integer, SnakePath> activePaths;
        private List<SnakePath> completedPaths;
        private Paint paint;
        private static final float MAX_STROKE_WIDTH = 40f;
        private static final float MIN_STROKE_WIDTH = 5f;
        private static final long FADE_DURATION = 1000; // 5 seconds

        public DrawingView(Context context) {
            super(context);
            activePaths = new HashMap<>();
            completedPaths = new ArrayList<>();
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            long currentTime = System.currentTimeMillis();

            for (SnakePath path : activePaths.values()) {
                path.draw(canvas, paint, currentTime, true);
            }

            Iterator<SnakePath> iterator = completedPaths.iterator();
            while (iterator.hasNext()) {
                SnakePath path = iterator.next();
                if (currentTime - path.getStartTime() > FADE_DURATION && path.getPoints().size() > 1) {
                    path.removeOldestPoint();
                    if (path.getPoints().isEmpty()) {
                        iterator.remove();
                        continue;
                    }
                }
                path.draw(canvas, paint, currentTime, false);
            }

            invalidate();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int action = event.getActionMasked();
            int pointerIndex = event.getActionIndex();
            int pointerId = event.getPointerId(pointerIndex);

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    // Only create a new path if one doesn't already exist for this pointer
                    if (!activePaths.containsKey(pointerId)) {
                        float x = event.getX(pointerIndex);
                        float y = event.getY(pointerIndex);
                        activePaths.put(pointerId, new SnakePath(x, y));
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    // Handle all pointers
                    for (int i = 0; i < event.getPointerCount(); i++) {
                        int id = event.getPointerId(i);
                        SnakePath path = activePaths.get(id);
                        if (path != null) {
                            path.addPoint(event.getX(i), event.getY(i));
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_CANCEL:
                    SnakePath path = activePaths.remove(pointerId);
                    if (path != null) {
                        completedPaths.add(path);
                    }
                    break;
            }

            invalidate();
            return true;
        }
    }

    private class SnakePath {
        private List<Point> points;
        private int color;
        private long startTime;

        public SnakePath(float x, float y) {
            points = new ArrayList<>();
            points.add(new Point(x, y, System.currentTimeMillis()));
            color = Color.HSVToColor(new float[]{(float) Math.random() * 360, 1, 1});
            startTime = System.currentTimeMillis();
        }

        public void addPoint(float x, float y) {
            points.add(new Point(x, y, System.currentTimeMillis()));
        }

        public void draw(Canvas canvas, Paint paint, long currentTime, boolean isActive) {
            if (points.size() < 2) return;

            Path path = new Path();
            path.moveTo(points.get(0).x, points.get(0).y);

            for (int i = 1; i < points.size(); i++) {
                Point prevPoint = points.get(i - 1);
                Point point = points.get(i);

                float midX = (prevPoint.x + point.x) / 2;
                float midY = (prevPoint.y + point.y) / 2;

                if (i == 1) {
                    path.lineTo(midX, midY);
                } else {
                    path.quadTo(prevPoint.x, prevPoint.y, midX, midY);
                }

                long pointAge = currentTime - point.time;
                float ageFactor = 1f - (float) pointAge / DrawingView.FADE_DURATION;
                ageFactor = Math.max(0f, Math.min(1f, ageFactor));

                float positionFactor = isActive ? (float)i / points.size() : ageFactor;
                float strokeWidth = DrawingView.MIN_STROKE_WIDTH + (DrawingView.MAX_STROKE_WIDTH - DrawingView.MIN_STROKE_WIDTH) * positionFactor;

                paint.setColor(Color.argb((int) (255 * ageFactor), Color.red(color), Color.green(color), Color.blue(color)));
                paint.setStrokeWidth(strokeWidth);

                canvas.drawPath(path, paint);
                path.reset();
                path.moveTo(midX, midY);
            }
        }

        public long getStartTime() {
            return startTime;
        }

        public List<Point> getPoints() {
            return points;
        }

        public void removeOldestPoint() {
            if (!points.isEmpty()) {
                points.remove(0);
                if (!points.isEmpty()) {
                    startTime = points.get(0).time;
                }
            }
        }
    }

    private class Point {
        float x, y;
        long time;

        Point(float x, float y, long time) {
            this.x = x;
            this.y = y;
            this.time = time;
        }
    }
}