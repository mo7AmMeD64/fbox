package com.fkbox.app.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.fkbox.app.R
import com.fkbox.app.data.moviebox.Item
import com.fkbox.app.ui.theme.DesignTokens

// ========================================================================
// COMPOSITION LOCALS
// ========================================================================

/** Shows a snackbar: (message, actionLabel, onAction). Provided by the root scaffold. */
val LocalSnack = staticCompositionLocalOf<(String, String?, (() -> Unit)?) -> Unit> { { _, _, _ -> } }

/** Current navigation state for adaptive layouts */
val LocalNavigationState = staticCompositionLocalOf<NavigationState> { NavigationState() }

data class NavigationState(
    val isTablet: Boolean = false,
    val isLandscape: Boolean = false,
    val windowWidth: Int = 0,
    val windowHeight: Int = 0,
)

// ========================================================================
// MODIFIERS & EXTENSIONS
// ========================================================================

/** Slight spring press-scale used on every tappable surface. */
@Composable
fun Modifier.pressScale(
    source: InteractionSource,
    pressedScale: Float = 0.96f,
): Modifier {
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pressScale",
    )
    return this.graphicsLayer { scaleX = scale; scaleY = scale }
}

/** Ripple + press-scale click for arbitrary layouts. */
@Composable
fun Modifier.clickableScale(
    onClick: () -> Unit,
    enabled: Boolean = true,
): Modifier {
    val source = remember { MutableInteractionSource() }
    return this
        .pressScale(source)
        .clickable(enabled = enabled, interactionSource = source, indication = null, onClick = onClick)
}

/** Standard screen horizontal padding */
@Composable
fun Modifier.screenHorizontalPadding() = this.padding(horizontal = DesignTokens.Spacing.ScreenHorizontal)

/** Standard screen vertical padding */
@Composable
fun Modifier.screenVerticalPadding() = this.padding(vertical = DesignTokens.Spacing.ScreenVertical)

/** Card internal padding */
@Composable
fun Modifier.cardPadding() = this.padding(DesignTokens.Spacing.Card)

/** Section spacing */
@Composable
fun Modifier.sectionSpacing() = this.padding(vertical = DesignTokens.Spacing.Section)

/** Item gap for lists/grids */
@Composable
fun Modifier.itemGap() = this.padding(DesignTokens.Spacing.ItemGap)

/** Safe area insets aware padding */
@Composable
fun Modifier.safePadding(
    top: Boolean = true,
    bottom: Boolean = true,
    start: Boolean = true,
    end: Boolean = true,
) = this
    .padding(top = if (top) DesignTokens.Spacing.ScreenVertical else 0.dp)
    .padding(bottom = if (bottom) DesignTokens.Spacing.ScreenVertical else 0.dp)
    .padding(start = if (start) DesignTokens.Spacing.ScreenHorizontal else 0.dp)
    .padding(end = if (end) DesignTokens.Spacing.ScreenHorizontal else 0.dp)

/** Standard container for screen content */
@Composable
fun Modifier.screenContainer() = this
    .fillMaxSize()
    .background(MaterialTheme.colorScheme.background)
    .safePadding()

/** Animated visibility with standard transitions */
val StandardEnter: EnterTransition = fadeIn(animationSpec = DesignTokens.Motion.SpringSmooth) +
    scaleIn(initialScale = 0.95f, animationSpec = DesignTokens.Motion.SpringSmooth)
val StandardExit: ExitTransition = fadeOut(animationSpec = DesignTokens.Motion.SpringSmooth) +
    scaleOut(targetScale = 0.95f, animationSpec = DesignTokens.Motion.SpringSmooth)

// ========================================================================
// TYPOGRAPHY COMPONENTS
// ========================================================================

/** 28sp section / screen heading (line height 1.3x). */
@Composable
fun Heading(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    style: String = "headlineMedium",
) {
    Text(
        text = text,
        modifier = modifier,
        textAlign = textAlign,
        color = MaterialTheme.colorScheme.onSurface,
        style = when (style) {
            "displayLarge" -> MaterialTheme.typography.displayLarge
            "displayMedium" -> MaterialTheme.typography.displayMedium
            "displaySmall" -> MaterialTheme.typography.displaySmall
            "headlineLarge" -> MaterialTheme.typography.headlineLarge
            "headlineMedium" -> MaterialTheme.typography.headlineMedium
            "headlineSmall" -> MaterialTheme.typography.headlineSmall
            "titleLarge" -> MaterialTheme.typography.titleLarge
            "titleMedium" -> MaterialTheme.typography.titleMedium
            "titleSmall" -> MaterialTheme.typography.titleSmall
            "bodyLarge" -> MaterialTheme.typography.bodyLarge
            "bodyMedium" -> MaterialTheme.typography.bodyMedium
            "bodySmall" -> MaterialTheme.typography.bodySmall
            "labelLarge" -> MaterialTheme.typography.labelLarge
            "labelMedium" -> MaterialTheme.typography.labelMedium
            "labelSmall" -> MaterialTheme.typography.labelSmall
            else -> MaterialTheme.typography.headlineMedium
        },
    )
}

/** Body text with consistent styling */
@Composable
fun Body(
    text: String,
    modifier: Modifier = Modifier,
    style: String = "bodyMedium",
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        modifier = modifier,
        style = when (style) {
            "bodyLarge" -> MaterialTheme.typography.bodyLarge
            "bodyMedium" -> MaterialTheme.typography.bodyMedium
            "bodySmall" -> MaterialTheme.typography.bodySmall
            "labelLarge" -> MaterialTheme.typography.labelLarge
            "labelMedium" -> MaterialTheme.typography.labelMedium
            "labelSmall" -> MaterialTheme.typography.labelSmall
            else -> MaterialTheme.typography.bodyMedium
        },
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

/** Metadata text - small, muted, for ratings, years, genres */
@Composable
fun Metadata(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

// ========================================================================
// IMAGE COMPONENTS
// ========================================================================

/** Image with a surfaceContainer placeholder; centre-cropped. */
@Composable
fun CoverImage(
    url: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    aspectRatio: Float = DesignTokens.Size.PosterAspectRatio,
    crossfade: Boolean = true,
    placeholder: @Composable (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .clip(DesignTokens.Shape.MD)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        if (!url.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = url,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = placeholder ?: {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(DesignTokens.Size.IconXL),
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.BrokenImage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(DesignTokens.Size.IconXL),
                        )
                    }
                },
            )
        } else {
            placeholder?.invoke() ?: Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(DesignTokens.Size.IconXL),
                )
            }
        }
    }
}

/** Backdrop image for hero sections - 16:9 aspect ratio */
@Composable
fun BackdropImage(
    url: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    overlay: Boolean = true,
) {
    Box(modifier = modifier.aspectRatio(DesignTokens.Size.BackdropAspectRatio)) {
        CoverImage(
            url = url,
            modifier = Modifier.fillMaxSize(),
            contentDescription = contentDescription,
            aspectRatio = DesignTokens.Size.BackdropAspectRatio,
        )
        if (overlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = DesignTokens.Gradients.HeroOverlay.map { (color, _) -> color }
                        )
                    )
            )
        }
    }
}

// ========================================================================
// RATING & BADGE COMPONENTS
// ========================================================================

@Composable
fun RatingBadge(
    rating: Double?,
    modifier: Modifier = Modifier,
    size: Int = 14,
    showIcon: Boolean = true,
) {
    if (rating == null) return
    Surface(
        modifier = modifier,
        shape = DesignTokens.Shape.Circle,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showIcon) {
                Icon(Icons.Rounded.Star, null, modifier = Modifier.size(size.dp))
                Spacer(Modifier.width(4.dp))
            }
            Text(
                String.format("%.1f", rating),
                style = MaterialTheme.typography.labelMedium.copy(fontSize = (size - 2).sp),
            )
        }
    }
}

/** Content type badge - Movie / Series / Episode */
@Composable
fun ContentTypeBadge(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Surface(
        modifier = modifier,
        shape = DesignTokens.Shape.Pill,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                Icon(it, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ========================================================================
// CARD COMPONENTS
// ========================================================================

/** Standard poster card used in grids and rows. */
@Composable
fun PosterCard(
    item: Item,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    showRating: Boolean = true,
) {
    val source = remember { MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()

    Card(
        onClick = onClick,
        enabled = true,
        modifier = modifier
            .pressScale(source, if (isPressed) 0.94f else 0.96f),
        shape = DesignTokens.Shape.MD,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        interactionSource = source,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            CoverImage(
                url = item.posterUrl,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
            )

            // Gradient overlay at bottom for text readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(80.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            0.0f to Color.Transparent,
                            1.0f to MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f),
                        )
                    )
            )

            if (showRating) {
                RatingBadge(
                    rating = item.imdb,
                    modifier = Modifier.align(Alignment.TopStart).padding(DesignTokens.Spacing.SM),
                )
            }

            trailing?.let {
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(DesignTokens.Spacing.SM)) {
                    it()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(DesignTokens.Spacing.MD),
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(DesignTokens.Spacing.XS))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ContentTypeBadge(
                        text = stringResource(if (item.isSeries) R.string.type_series else R.string.type_movie),
                    )
                }
            }
        }
    }
}

/** Horizontal poster card for "Continue Watching" row */
@Composable
fun HorizontalPosterCard(
    item: Item,
    progress: Float = 0f,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.width(120.dp),
    trailing: (@Composable () -> Unit)? = null,
) {
    val source = remember { MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()

    Card(
        onClick = onClick,
        modifier = modifier
            .pressScale(source, if (isPressed) 0.96f else 0.98f),
        shape = DesignTokens.Shape.MD,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        interactionSource = source,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.aspectRatio(DesignTokens.Size.PosterAspectRatio)) {
                CoverImage(
                    url = item.posterUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                )
                trailing?.let {
                    Box(modifier = Modifier.align(Alignment.TopEnd).padding(DesignTokens.Spacing.SM)) {
                        it()
                    }
                }
            }
            Column(modifier = Modifier.padding(DesignTokens.Spacing.MD)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (progress > 0) {
                    Spacer(Modifier.height(DesignTokens.Spacing.XS))
                    Column(Modifier.fillMaxWidth()) {
                        Row {
                            Text(
                                text = "${(progress * 100).toInt()}% watched",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        androidx.compose.material3.LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

/** Featured hero card for home screen */
@Composable
fun FeaturedHeroCard(
    item: Item?,
    onPlay: () -> Unit,
    onDetails: () -> Unit,
    onFavorite: () -> Unit,
    isFavorite: Boolean = false,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    val source = remember { MutableInteractionSource() }

    Card(
        onClick = onDetails,
        modifier = modifier
            .height(DesignTokens.Size.HeroHeight)
            .pressScale(source),
        shape = DesignTokens.Shape.LG,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        interactionSource = source,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            CoverImage(
                url = item?.posterUrl,
                contentDescription = item?.title,
                modifier = Modifier.fillMaxSize(),
                aspectRatio = DesignTokens.Size.BackdropAspectRatio,
            )

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = DesignTokens.Gradients.HeroOverlay.map { (color, _) -> color }
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(DesignTokens.Spacing.LG),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.Start,
            ) {
                if (item != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.MD),
                    ) {
                        // Play button
                        Button(
                            onClick = onPlay,
                            modifier = Modifier
                                .padding(bottom = DesignTokens.Spacing.SM)
                                .weight(1f),
                            shape = DesignTokens.Shape.MD,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = DesignTokens.Spacing.LG, vertical = DesignTokens.Spacing.MD),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(Icons.Rounded.PlayArrow, null, modifier = Modifier.size(DesignTokens.Size.IconMD))
                                Spacer(Modifier.width(DesignTokens.Spacing.SM))
                                Text(
                                    stringResource(R.string.play_movie),
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        }

                        // More details button
                        Button(
                            onClick = onDetails,
                            modifier = Modifier
                                .padding(bottom = DesignTokens.Spacing.SM)
                                .weight(1f),
                            shape = DesignTokens.Shape.MD,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f),
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        ) {
                            Text(
                                stringResource(R.string.more_details),
                                modifier = Modifier.padding(vertical = DesignTokens.Spacing.MD),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }

                        // Favorite button
                        FilledTonalIconButton(
                            onClick = onFavorite,
                            modifier = Modifier.padding(bottom = DesignTokens.Spacing.SM),
                            shape = DesignTokens.Shape.Circle,
                        ) {
                            Icon(
                                Icons.Rounded.Favorite,
                                contentDescription = stringResource(if (isFavorite) R.string.unsave else R.string.save),
                                tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    // Metadata row
                    Spacer(Modifier.height(DesignTokens.Spacing.MD))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.LG),
                    ) {
                        item.imdb?.let {
                            RatingBadge(rating = it, showIcon = true)
                        }
                        if (item.isSeries) {
                            ContentTypeBadge(text = stringResource(R.string.type_series))
                        } else {
                            ContentTypeBadge(text = stringResource(R.string.type_movie))
                        }
                    }
                }
            }
        }
    }
}

// ========================================================================
// LIST / GRID HELPERS
// ========================================================================

/** Standard section header with optional action */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.Spacing.ScreenHorizontal, vertical = DesignTokens.Spacing.SM),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Heading(text = title, style = "titleLarge")
        actionLabel?.let { label ->
            onAction?.let { action ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickableScale(action)
                        .padding(end = DesignTokens.Spacing.ScreenHorizontal),
                )
            }
        }
    }
}

/** Skeleton placeholder for loading states */
@Composable
fun SkeletonPoster(
    modifier: Modifier = Modifier
        .width(150.dp)
        .aspectRatio(DesignTokens.Size.PosterAspectRatio),
) {
    Box(
        modifier = modifier
            .clip(DesignTokens.Shape.MD)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    ) {
        // Shimmer animation would go here in a full implementation
    }
}

@Composable
fun SkeletonHorizontalPoster(
    modifier: Modifier = Modifier.width(120.dp),
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .aspectRatio(DesignTokens.Size.PosterAspectRatio)
                .clip(DesignTokens.Shape.MD)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        )
        Spacer(Modifier.height(DesignTokens.Spacing.MD))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(DesignTokens.Shape.SM)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        )
        Spacer(Modifier.height(DesignTokens.Spacing.XS))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(DesignTokens.Shape.SM)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        )
    }
}

// ========================================================================
// INTERACTIVE COMPONENTS
// ========================================================================

/** Favorite toggle button with animated heart */
@Composable
fun FavoriteButton(
    saved: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = DesignTokens.Size.IconMD,
) {
    val source = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (saved) 1.15f else 1f,
        animationSpec = DesignTokens.Motion.SpringBouncy,
        label = "favoriteScale",
    )

    FilledTonalIconButton(
        onClick = onClick,
        modifier = modifier
            .pressScale(source)
            .graphicsLayer { scaleX = scale; scaleY = scale },
        interactionSource = source,
        shape = DesignTokens.Shape.Circle,
        colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = if (saved) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = if (saved) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Icon(
            Icons.Rounded.Favorite,
            contentDescription = stringResource(if (saved) R.string.unsave else R.string.save),
            modifier = Modifier.size(size),
        )
    }
}

/** Primary action button */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val source = remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .pressScale(source)
            .height(56.dp)
            .fillMaxWidth(),
        shape = DesignTokens.Shape.MD,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        ),
        interactionSource = source,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = DesignTokens.Spacing.LG),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(DesignTokens.Size.IconMD),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 3.dp,
                )
            } else {
                icon?.let {
                    Icon(it, null, modifier = Modifier.size(DesignTokens.Size.IconMD))
                    Spacer(Modifier.width(DesignTokens.Spacing.SM))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

/** Secondary action button */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val source = remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .pressScale(source)
            .height(56.dp)
            .fillMaxWidth(),
        shape = DesignTokens.Shape.MD,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        ),
        interactionSource = source,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = DesignTokens.Spacing.LG),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                Icon(it, null, modifier = Modifier.size(DesignTokens.Size.IconMD))
                Spacer(Modifier.width(DesignTokens.Spacing.SM))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

/** Outlined button */
@Composable
fun OutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val source = remember { MutableInteractionSource() }
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .pressScale(source)
            .height(56.dp)
            .fillMaxWidth(),
        shape = DesignTokens.Shape.MD,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        ),
        interactionSource = source,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = DesignTokens.Spacing.LG),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                Icon(it, null, modifier = Modifier.size(DesignTokens.Size.IconMD))
                Spacer(Modifier.width(DesignTokens.Spacing.SM))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

// ========================================================================
// FEEDBACK COMPONENTS
// ========================================================================

/** Centered loader with proper sizing */
@Composable
fun LoadingBlock(
    modifier: Modifier = Modifier,
    size: Int = 48,
    label: String? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().height(140.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(size.dp),
            strokeWidth = 4.dp,
            color = MaterialTheme.colorScheme.primary,
        )
        label?.let { text ->
            Spacer(Modifier.height(DesignTokens.Spacing.MD))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Empty state with icon, title, description, and optional action */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(DesignTokens.Spacing.XXL)
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.MD),
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = DesignTokens.Spacing.LG),
            )
        }
        actionLabel?.let { label ->
            onAction?.let { action ->
                Spacer(Modifier.height(DesignTokens.Spacing.MD))
                PrimaryButton(text = label, onClick = action, modifier = Modifier.width(200.dp))
            }
        }
    }
}

/** Error state with retry action */
@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    icon: ImageVector = Icons.Filled.CloudOff,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(DesignTokens.Spacing.XXL)
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.MD),
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
        )
        Text(
            text = stringResource(R.string.error_generic, message),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        onRetry?.let {
            PrimaryButton(
                text = stringResource(R.string.retry),
                onClick = it,
                modifier = Modifier.width(200.dp),
                icon = Icons.Rounded.Refresh,
            )
        }
    }
}

/** Indeterminate circular progress with 4dp round-capped strokes. */
@Composable
fun WavyProgress(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier,
        strokeWidth = 4.dp,
        color = MaterialTheme.colorScheme.primary,
    )
}

// ========================================================================
// DIVIDERS & SEPARATORS
// ========================================================================

@Composable
fun InsetDivider(
    modifier: Modifier = Modifier,
    horizontalPadding: Boolean = true,
) {
    androidx.compose.material3.Divider(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (horizontalPadding) DesignTokens.Spacing.ScreenHorizontal else 0.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        thickness = 1.dp,
    )
}

// ========================================================================
// CHIPS & FILTERS
// ========================================================================

/** Filter chip for genre selection */
@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    val source = remember { MutableInteractionSource() }
    val backgroundColor = if (selected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surfaceContainerHighest
    val contentColor = if (selected)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    androidx.compose.material3.FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier
            .pressScale(source)
            .height(40.dp),
        shape = DesignTokens.Shape.Pill,
        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
            containerColor = backgroundColor,
            selectedContainerColor = backgroundColor,
            labelColor = contentColor,
            selectedLabelColor = contentColor,
        ),
        interactionSource = source,
        leadingIcon = leadingIcon?.let {
            { Icon(it, null, modifier = Modifier.size(DesignTokens.Size.IconSM), tint = contentColor) }
        },
        trailingIcon = trailingIcon?.let {
            { Icon(it, null, modifier = Modifier.size(DesignTokens.Size.IconSM), tint = contentColor) }
        },
        label = { Text(label, style = MaterialTheme.typography.labelMedium, color = contentColor) },
    )
}

/** Genre chip for display (non-interactive) */
@Composable
fun GenreChip(
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = DesignTokens.Shape.Pill,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

// ========================================================================
// LAZY LIST EXTENSIONS
// ========================================================================

fun LazyListScope.sectionSpacing() {
    item { Spacer(Modifier.height(DesignTokens.Spacing.Section)) }
}

fun LazyListScope.itemSpacing() {
    item { Spacer(Modifier.height(DesignTokens.Spacing.ItemGap)) }
}