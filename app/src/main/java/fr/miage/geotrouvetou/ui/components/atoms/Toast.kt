package fr.miage.geotrouvetou.ui.components.atoms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.annotation.ColorRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import fr.miage.geotrouvetou.R

enum class ToastType(
    val icon: ImageVector,
    val iconDescription: String,
    @param:ColorRes val color: Int,
    @param:ColorRes val backgroundColor: Int,
) {
    Success(Icons.Rounded.Check, "Succès", R.color.success_500, R.color.success_transparent),
    Warning(Icons.Rounded.Warning, "Avertissement", R.color.warning_500, R.color.warning_transparent),
}

@Composable
fun Toast(
    title: String = "Succès !",
    description: String = "Description du toast",
    modifier: Modifier = Modifier.zIndex(99999f),
    type: ToastType = ToastType.Success,
    duration: Int = 3000,
    initialProgress: Float = 0f,
    key: Any = Unit
) {
    val transitionState = remember {
        MutableTransitionState(false)
    }
    val progress = remember { Animatable(initialProgress) }

    LaunchedEffect(key) {
        if (duration > 0) {
            progress.snapTo(0f)
            transitionState.targetState = true
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = duration,
                    easing = LinearEasing
                )
            )
            transitionState.targetState = false
        } else {
            transitionState.targetState = true
        }
    }

    if (transitionState.currentState || transitionState.targetState) {
        Popup(
            alignment = Alignment.TopCenter,
            properties = PopupProperties(
                focusable = false,
                dismissOnClickOutside = false,
                dismissOnBackPress = false
            )
        ) {
            AnimatedVisibility(
                visibleState = transitionState,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it })
            ) {
                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .background(Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = colorResource(id = type.backgroundColor),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = type.icon,
                                contentDescription = type.iconDescription,
                                tint = colorResource(id = type.color),
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = title,
                                color = colorResource(id = R.color.text_darker),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = description,
                                color = colorResource(id = R.color.text_light),
                                fontSize = 16.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .background(colorResource(id = type.backgroundColor))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.value)
                                .fillMaxHeight()
                                .background(colorResource(id = type.color))
                        )
                    }
                }
            }
        }
    }
}
