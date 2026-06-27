package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.database.SavedScript
import com.example.ui.theme.*
import com.example.viewmodel.SceneItem
import com.example.viewmodel.UiState
import com.example.viewmodel.VideoGeneratorViewModel

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) { innerPadding ->
                    AppScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// Localization data definitions
data class AppLabels(
    val title: String,
    val subtitle: String,
    val textToVideoTab: String,
    val imageToVideoTab: String,
    val historyTab: String,
    val conceptLabel: String,
    val conceptPlaceholder: String,
    val styleLabel: String,
    val durationLabel: String,
    val formatLabel: String,
    val generateBtn: String,
    val generating: String,
    val choosePreset: String,
    val customDescLabel: String,
    val customDescPlaceholder: String,
    val physicsLabel: String,
    val cameraLabel: String,
    val resultTitle: String,
    val copyBtn: String,
    val copiedMsg: String,
    val masterPromptTitle: String,
    val sceneBreakdownTitle: String,
    val ctaTitle: String,
    val visualPromptLabel: String,
    val audioLabel: String,
    val textOnScreenLabel: String,
    val emptyHistory: String,
    val clearHistoryBtn: String,
    val deleteBtn: String,
    val savedInLibrary: String,
    val selectImgPrompt: String,
    val arabicLangBtn: String,
    val englishLangBtn: String,
    val customImageTab: String
)

val englishLabels = AppLabels(
    title = "AI Video & Ad Generator",
    subtitle = "100% Free Next-Gen AI Video Prompts & Scripts",
    textToVideoTab = "Text-to-Video/Ad",
    imageToVideoTab = "Image-to-Video",
    historyTab = "My Library",
    conceptLabel = "Product / Ad Concept",
    conceptPlaceholder = "e.g., Smart energy drink for gamers that boosts focus and reflex speed...",
    styleLabel = "Visual Style & Vibe",
    durationLabel = "Target Duration",
    formatLabel = "Platform / Format",
    generateBtn = "Generate Cinematic Campaign 🚀",
    generating = "Creating Masterpiece... Please Wait ✨",
    choosePreset = "Choose Preset Base Image",
    customDescLabel = "Custom Image Description",
    customDescPlaceholder = "Describe the image you want to animate in detail...",
    physicsLabel = "Physics & Fluid Dynamics",
    cameraLabel = "Camera Motion & Framing",
    resultTitle = "🎬 Generated Campaign Breakdown",
    copyBtn = "Copy Prompt",
    copiedMsg = "Copied to clipboard!",
    masterPromptTitle = "🚀 Master AI Video Prompt (Copy/Paste this into Sora, Runway, Kling)",
    sceneBreakdownTitle = "🎥 Scene-by-Scene Script",
    ctaTitle = "🎯 Recommended Call to Action (CTA)",
    visualPromptLabel = "Visual & Camera Prompt",
    audioLabel = "Audio & Voiceover",
    textOnScreenLabel = "Text on Screen",
    emptyHistory = "Your Library is empty. Generate a campaign to get started!",
    clearHistoryBtn = "Clear All",
    deleteBtn = "Delete",
    savedInLibrary = "Saved automatically in your library",
    selectImgPrompt = "Select an image above or choose Custom to describe one",
    arabicLangBtn = "العربية 🇸🇦",
    englishLangBtn = "English 🇬🇧",
    customImageTab = "Custom Image"
)

val arabicLabels = AppLabels(
    title = "صانع الفيديو والإعلان بالذكاء الاصطناعي",
    subtitle = "صناعة نصوص ومطالبات فيديو احترافية 100% مجاناً",
    textToVideoTab = "نص إلى فيديو/إعلان",
    imageToVideoTab = "صورة إلى فيديو",
    historyTab = "مكتبتي",
    conceptLabel = "فكرة المنتج أو الإعلان",
    conceptPlaceholder = "مثال: مشروب طاقة ذكي للاعبين يعزز التركيز وسرعة الاستجابة...",
    styleLabel = "الأسلوب البصري والمزاج",
    durationLabel = "المدة المستهدفة",
    formatLabel = "المنصة / المقاس",
    generateBtn = "إنشاء الحملة الإعلانية 🚀",
    generating = "جاري ابتكار التحفة الفنية... يرجى الانتظار ✨",
    choosePreset = "اختر صورة أساسية جاهزة",
    customDescLabel = "وصف الصورة المخصصة",
    customDescPlaceholder = "صف الصورة التي ترغب في تحريكها وتجسيدها بالتفصيل...",
    physicsLabel = "ديناميكيات الحركة والفيزياء",
    cameraLabel = "حركة الكاميرا والتأطير",
    resultTitle = "🎬 تفاصيل الحملة الإعلانية المنتجة",
    copyBtn = "نسخ المطالبة",
    copiedMsg = "تم النسخ إلى الحافظة!",
    masterPromptTitle = "🚀 المطالبة الرئيسية للذكاء الاصطناعي (انسخها إلى Sora, Runway, Kling)",
    sceneBreakdownTitle = "🎥 سيناريو المشاهد بالتفصيل",
    ctaTitle = "🎯 العبارة الختامية الموصى بها (CTA)",
    visualPromptLabel = "الوصف البصري وحركة الكاميرا",
    audioLabel = "الصوت والتعليق الصوتي",
    textOnScreenLabel = "النصوص على الشاشة",
    emptyHistory = "مكتبتك فارغة حالياً. ابدأ بإنشاء حملتك الإعلانية الأولى!",
    clearHistoryBtn = "مسح الكل",
    deleteBtn = "حذف",
    savedInLibrary = "تم الحفظ تلقائياً في مكتبتك الخاصة",
    selectImgPrompt = "اختر صورة من الأعلى أو حدد مخصص لكتابة وصفك الخاص",
    arabicLangBtn = "العربية 🇸🇦",
    englishLangBtn = "English 🇬🇧",
    customImageTab = "صورة مخصصة"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    modifier: Modifier = Modifier,
    viewModel: VideoGeneratorViewModel = viewModel()
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Observe State
    val selectedTab by viewModel.selectedTab.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val historyList by viewModel.history.collectAsState()

    // Inputs Text-to-Video
    val textConcept by viewModel.textConcept.collectAsState()
    val textStyle by viewModel.textStyle.collectAsState()
    val textDuration by viewModel.textDuration.collectAsState()
    val textPlatform by viewModel.textPlatform.collectAsState()
    val textLanguage by viewModel.textLanguage.collectAsState()

    // Inputs Image-to-Video
    val imageDescription by viewModel.imageDescription.collectAsState()
    val imagePhysics by viewModel.imagePhysics.collectAsState()
    val imageCamera by viewModel.imageCamera.collectAsState()
    val selectedPresetIndex by viewModel.selectedPresetIndex.collectAsState()
    val imageLanguage by viewModel.imageLanguage.collectAsState()

    // Active Labels (Default to Arabic if any state matches or if switched)
    val currentLang = if (selectedTab == 0) textLanguage else imageLanguage
    val labels = if (currentLang == "Arabic") arabicLabels else englishLabels
    val isRtl = currentLang == "Arabic"

    CompositionLocalProvider(
        androidx.compose.ui.platform.LocalLayoutDirection provides if (isRtl) {
            androidx.compose.ui.unit.LayoutDirection.Rtl
        } else {
            androidx.compose.ui.unit.LayoutDirection.Ltr
        }
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Hero Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = "AI Studio Hero Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient overlay for visual blend and cinematic aesthetic
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                                        MaterialTheme.colorScheme.background
                                    )
                                )
                            )
                    )
                    // Decorative chips & title info on top
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_app_icon),
                                    contentDescription = "Logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = labels.title,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = labels.subtitle,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Language Toggles
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    val activeLang = if (selectedTab == 0) textLanguage else imageLanguage
                    
                    TextButton(
                        onClick = {
                            if (selectedTab == 0) viewModel.textLanguage.value = "English"
                            else viewModel.imageLanguage.value = "English"
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (activeLang == "English") MaterialTheme.colorScheme.primary else CosmicMuted
                        ),
                        modifier = Modifier.testTag("english_lang_toggle")
                    ) {
                        Text(labels.englishLangBtn, fontWeight = if (activeLang == "English") FontWeight.Bold else FontWeight.Normal)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = {
                            if (selectedTab == 0) viewModel.textLanguage.value = "Arabic"
                            else viewModel.imageLanguage.value = "Arabic"
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (activeLang == "Arabic") MaterialTheme.colorScheme.primary else CosmicMuted
                        ),
                        modifier = Modifier.testTag("arabic_lang_toggle")
                    ) {
                        Text(labels.arabicLangBtn, fontWeight = if (activeLang == "Arabic") FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }

            // Tabs Selector
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TabItem(
                        title = labels.textToVideoTab,
                        isSelected = selectedTab == 0,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("text_to_video_tab"),
                        onClick = { viewModel.selectTab(0) }
                    )
                    TabItem(
                        title = labels.imageToVideoTab,
                        isSelected = selectedTab == 1,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("image_to_video_tab"),
                        onClick = { viewModel.selectTab(1) }
                    )
                    TabItem(
                        title = labels.historyTab,
                        isSelected = selectedTab == 2,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("history_tab"),
                        onClick = { viewModel.selectTab(2) }
                    )
                }
            }

            // Dynamic Form based on selected Tab
            when (selectedTab) {
                0 -> { // Text to Video Form
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = labels.conceptLabel,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = textConcept,
                                onValueChange = { viewModel.textConcept.value = it },
                                placeholder = { Text(labels.conceptPlaceholder, color = CosmicMuted) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("concept_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Grid of configurations
                            Text(
                                text = labels.styleLabel,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            val styleOptions = listOf(
                                "Cinematic 3D", "Cyberpunk Neon", "3D Pixar Animation", 
                                "Luxury Minimalist", "Retro Vintage Film"
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(styleOptions) { styleOpt ->
                                    PresetChip(
                                        label = styleOpt,
                                        isSelected = textStyle == styleOpt,
                                        onClick = { viewModel.textStyle.value = styleOpt }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = labels.durationLabel,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    val durationOptions = listOf("10 Seconds", "15 Seconds", "30 Seconds")
                                    var durationExpanded by remember { mutableStateOf(false) }
                                    ExposedDropdownMenuBox(
                                        expanded = durationExpanded,
                                        onExpandedChange = { durationExpanded = !durationExpanded }
                                    ) {
                                        OutlinedTextField(
                                            value = textDuration,
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = {
                                                Icon(Icons.Default.ArrowDropDown, "Open dropdown")
                                            },
                                            modifier = Modifier
                                                .menuAnchor()
                                                .fillMaxWidth()
                                                .clickable { durationExpanded = true },
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        DropdownMenu(
                                            expanded = durationExpanded,
                                            onDismissRequest = { durationExpanded = false },
                                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                        ) {
                                            durationOptions.forEach { opt ->
                                                DropdownMenuItem(
                                                    text = { Text(opt) },
                                                    onClick = {
                                                        viewModel.textDuration.value = opt
                                                        durationExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = labels.formatLabel,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    val formatOptions = listOf("TikTok / Reels (9:16)", "YouTube (16:9)", "Square (1:1)")
                                    var formatExpanded by remember { mutableStateOf(false) }
                                    ExposedDropdownMenuBox(
                                        expanded = formatExpanded,
                                        onExpandedChange = { formatExpanded = !formatExpanded }
                                    ) {
                                        OutlinedTextField(
                                            value = textPlatform,
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = {
                                                Icon(Icons.Default.ArrowDropDown, "Open dropdown")
                                            },
                                            modifier = Modifier
                                                .menuAnchor()
                                                .fillMaxWidth()
                                                .clickable { formatExpanded = true },
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        DropdownMenu(
                                            expanded = formatExpanded,
                                            onDismissRequest = { formatExpanded = false },
                                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                        ) {
                                            formatOptions.forEach { opt ->
                                                DropdownMenuItem(
                                                    text = { Text(opt) },
                                                    onClick = {
                                                        viewModel.textPlatform.value = opt
                                                        formatExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Generate Button
                            Button(
                                onClick = { viewModel.generateTextToVideo() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("generate_campaign_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = labels.generateBtn,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> { // Image to Video Form
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = labels.choosePreset,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Preset Images row
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                item {
                                    PresetImageCard(
                                        drawableId = R.drawable.img_preset_cyberpunk,
                                        title = if (currentLang == "Arabic") "نيون سايبربانك" else "Cyberpunk City",
                                        isSelected = selectedPresetIndex == 0,
                                        onClick = { viewModel.selectedPresetIndex.value = 0 }
                                    )
                                }
                                item {
                                    PresetImageCard(
                                        drawableId = R.drawable.img_preset_perfume,
                                        title = if (currentLang == "Arabic") "إعلان العطور" else "Cosmetics/Perfume",
                                        isSelected = selectedPresetIndex == 1,
                                        onClick = { viewModel.selectedPresetIndex.value = 1 }
                                    )
                                }
                                item {
                                    PresetImageCard(
                                        drawableId = R.drawable.img_preset_dragon,
                                        title = if (currentLang == "Arabic") "تنين ثلاثي الأبعاد" else "Pixar Dragon",
                                        isSelected = selectedPresetIndex == 2,
                                        onClick = { viewModel.selectedPresetIndex.value = 2 }
                                    )
                                }
                                item {
                                    PresetCustomCard(
                                        title = labels.customImageTab,
                                        isSelected = selectedPresetIndex == 3,
                                        onClick = { viewModel.selectedPresetIndex.value = 3 }
                                    )
                                }
                            }

                            // Custom Description if Custom selected
                            AnimatedVisibility(
                                visible = selectedPresetIndex == 3,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = labels.customDescLabel,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    OutlinedTextField(
                                        value = imageDescription,
                                        onValueChange = { viewModel.imageDescription.value = it },
                                        placeholder = { Text(labels.customDescPlaceholder, color = CosmicMuted) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("image_desc_input"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }

                            // Physics option
                            Text(
                                text = labels.physicsLabel,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            val physicsOptions = listOf(
                                "Water Ripples", "Wind Breeze", "Dynamic Particles", "Subtle Float", "Explosive Splash"
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(physicsOptions) { opt ->
                                    PresetChip(
                                        label = opt,
                                        isSelected = imagePhysics == opt,
                                        onClick = { viewModel.imagePhysics.value = opt }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Camera option
                            Text(
                                text = labels.cameraLabel,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            val cameraOptions = listOf(
                                "Slow-motion Zoom", "Cinematic Pan", "Orbit Sweep", "Push-In", "Crane Shot"
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(cameraOptions) { opt ->
                                    PresetChip(
                                        label = opt,
                                        isSelected = imageCamera == opt,
                                        onClick = { viewModel.imageCamera.value = opt }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Generate Button
                            Button(
                                onClick = { viewModel.generateImageToVideo() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("generate_image_to_video_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = labels.generateBtn,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> { // Library History Tab
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${labels.historyTab} (${historyList.size})",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (historyList.isNotEmpty()) {
                                    TextButton(
                                        onClick = { viewModel.clearHistory() },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(labels.clearHistoryBtn)
                                    }
                                }
                            }

                            if (historyList.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.List,
                                            contentDescription = null,
                                            tint = CosmicMuted,
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = labels.emptyHistory,
                                            color = CosmicMuted,
                                            textAlign = TextAlign.Center,
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(horizontal = 32.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    items(historyList, key = { it.id }) { script ->
                        HistoryScriptCard(
                            script = script,
                            labels = labels,
                            onDelete = { viewModel.deleteScript(script) },
                            onCopyPrompt = {
                                clipboardManager.setText(AnnotatedString(script.masterPrompt))
                                Toast.makeText(context, labels.copiedMsg, Toast.LENGTH_SHORT).show()
                            },
                            parseScenes = { viewModel.parseScenes(it) }
                        )
                    }
                }
            }

            // Results UI State overlay/holder
            if (selectedTab != 2) {
                item {
                    when (val state = uiState) {
                        is UiState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = labels.generating,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                        is UiState.Success -> {
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = spring()) + expandVertically()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    ResultContainer(
                                        script = state.script,
                                        labels = labels,
                                        onCopyPrompt = {
                                            clipboardManager.setText(AnnotatedString(state.script.masterPrompt))
                                            Toast.makeText(context, labels.copiedMsg, Toast.LENGTH_SHORT).show()
                                        },
                                        parseScenes = { viewModel.parseScenes(it) }
                                    )
                                }
                            }
                        }
                        is UiState.Error -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                                    .border(1.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = state.message,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        else -> { /* Idle */ }
                    }
                }
            }
        }
    }
}

// --- SUB-COMPOSABLES ---

@Composable
fun TabItem(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else CosmicMuted,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) MaterialTheme.colorScheme.primary else CosmicOnSurface,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}

@Composable
fun PresetImageCard(
    drawableId: Int,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .size(110.dp)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = drawableId),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                        )
                    )
            )
            Text(
                text = title,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(6.dp),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PresetCustomCard(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .size(110.dp)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else CosmicMuted,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else CosmicOnSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ResultContainer(
    script: SavedScript,
    labels: AppLabels,
    onCopyPrompt: () -> Unit,
    parseScenes: (String) -> List<SceneItem>
) {
    val scenes = parseScenes(script.sceneBreakdownJson)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Campaign info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = labels.resultTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = script.duration,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = script.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Master Prompt Area
            Text(
                text = labels.masterPromptTitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = script.masterPrompt,
                        fontSize = 13.sp,
                        color = CosmicOnSurface,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onCopyPrompt,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("copy_master_prompt_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(labels.copyBtn, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Scene-by-Scene Script
            if (scenes.isNotEmpty()) {
                Text(
                    text = labels.sceneBreakdownTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                scenes.forEachIndexed { idx, scene ->
                    SceneCard(idx + 1, scene, labels)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Call to Action
            if (script.callToAction.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = labels.ctaTitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = script.callToAction,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CosmicOnBackground,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = labels.savedInLibrary,
                    color = CosmicMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun SceneCard(num: Int, scene: SceneItem, labels: AppLabels) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scene $num",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp
                )
                Card(
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = scene.timeRange,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Visual Prompt
            Text(
                text = labels.visualPromptLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CosmicMuted
            )
            Text(
                text = scene.visualPrompt,
                fontSize = 13.sp,
                color = CosmicOnSurface,
                lineHeight = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Audio VO
            if (scene.audioVO.isNotEmpty()) {
                Text(
                    text = labels.audioLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicMuted
                )
                Text(
                    text = "\"${scene.audioVO}\"",
                    fontSize = 13.sp,
                    color = CosmicOnSurface,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Text on Screen
            if (scene.textOnScreen.isNotEmpty()) {
                Text(
                    text = labels.textOnScreenLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicMuted
                )
                Text(
                    text = scene.textOnScreen,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun HistoryScriptCard(
    script: SavedScript,
    labels: AppLabels,
    onDelete: () -> Unit,
    onCopyPrompt: () -> Unit,
    parseScenes: (String) -> List<SceneItem>
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(
                1.dp,
                if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(14.dp)
            )
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = script.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = script.concept,
                        fontSize = 12.sp,
                        color = CosmicMuted,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Card(
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    ) {
                        Text(
                            text = script.duration,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_script_button")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = labels.deleteBtn,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    ResultContainer(
                        script = script,
                        labels = labels,
                        onCopyPrompt = onCopyPrompt,
                        parseScenes = parseScenes
                    )
                }
            }
        }
    }
}
