package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * مؤشر دائري فني حديث يوضح نسبة استهلاك المصاريف بالنسبة للإيرادات كدائرة مدمجة ممتلئة
 */
@Composable
fun CircularFinancialIndicator(
    revenue: Double,
    expenses: Double,
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.error,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val total = if (revenue == 0.0) 1.0 else revenue
    val percentage = (expenses / total).coerceIn(0.0, 1.0).toFloat()
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 1000),
        label = "percentage"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(140.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(
                (size.width - diameter) / 2,
                (size.height - diameter) / 2
            )
            val canvasSize = Size(diameter, diameter)

            // خلفية المؤشر
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = canvasSize,
                style = Stroke(width = strokeWidth)
            )

            // نسبة المصاريف العكسية
            drawArc(
                color = if (percentage > 0.8f) secondaryColor else primaryColor,
                startAngle = -90f,
                sweepAngle = animatedPercentage * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = canvasSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "${(percentage * 100).toInt()}%",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "نسبة الصرف",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * مخطط بياني شريطي مخصص للمصاريف حسب الفئة بأسلوب ميكانيكي جميل
 */
@Composable
fun ExpensesCategoryBarChart(
    categoryCosts: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    val totalExpenses = categoryCosts.values.sum()
    val colorMap = remember {
        mapOf(
            "مواد" to Color(0xFFE91E63),
            "أجور عمالة" to Color(0xFFFF9800),
            "مقاول باطن" to Color(0xFF2196F3),
            "معدات" to Color(0xFF9C27B0),
            "مصروف عمومي وإداري" to Color(0xFF4CAF50),
            "مستخلصات عملاء" to Color(0xFF009688)
        )
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (categoryCosts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لا توجد مصروفات مسجلة بعد لعرض المخطط التجاري",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            categoryCosts.forEach { (category, amount) ->
                val ratio = if (totalExpenses > 0) (amount / totalExpenses).toFloat() else 0f
                val color = colorMap[category] ?: MaterialTheme.colorScheme.secondary
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(color, CircleShape)
                            )
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "${String.format("%,.0f", amount)} ر.س (${(ratio * 100).toInt()}%)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // شريط التقدم الفني المخصص
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(ratio)
                                .fillMaxHeight()
                                .background(
                                    color = color,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}
