# تقرير تأسيس «لعبتي — Lo3bati» على Star2D

## ما اختُبر فعليًا وما لم يُختبر
**اختُبر (محليًا، بأدوات Python فقط):**
- `python3 tools/make_template.py check` → «template is up to date» قبل التعديل وبعده، أي أن تعديلاتي لم تُبطل قالب التصدير الجاهز.
- `python3 tools/verify_template.py` → «template check OK» (314 صنفًا في game.zip، ملفا dex). اختبار ترميز الـmanifest **تخطّاه الأداة** لغياب java/lib_7.jar/android.jar.
- `tools/test_buttons.py` → نجح.
- تحقق من سلامة ملفات XML المعدّلة (parse ناجح).

**لم يُختبر (عائق بيئة، لا عائق في المشروع):**
- **لم يُبنَ التطبيق ولا أي APK.** بيئتي بلا Android SDK ولا Gradle ولا javac ولا شبكة (تعذّر حتى تنزيل Gradle 9.2.1 والمكتبات).
- **لم يُصدَّر APK للعبة تجريبية ولم يُثبَّت أي شيء على جهاز.** سلامة التصدير تستند إلى قالب Star2D الجاهز وفحوصه فقط.
- بقية اختبارات `tools/test_*.py` تتطلب ترجمة `:template` أو javac فلم تعمل.
- لم أختبر ظهور النص العربي في الشاشات المعدّلة (الشاشات الأصلية native مع TextView؛ أما تسميات LibGDX فاستخدمتُ فيها «Lo3bati» اللاتيني تفاديًا لفقدان الحروف).

## الملفات المعدّلة (فقط)
| الملف | التغيير |
|---|---|
| `app/src/main/res/values/strings.xml` | `app_name` ← «لعبتي — Lo3bati» |
| `app/src/main/res/layout/activity_main.xml` | عنوان الشاشة الأولى |
| `app/src/main/res/layout/loading_screen.xml` | «لعبتي — مبني على Star2D» (نُسبة المصدر محفوظة) |
| `app/src/main/res/layout/home_screen.xml` | عنوان الشاشة الرئيسية |
| `app/.../editor/ui/ProjectsListStage.java` | تسميتان: «Welcome to Lo3bati» و«Lo3bati» + تعليق إشعار |
| `app/src/main/assets/i18n/strings*.properties` (7 ملفات) | قيمة المفتاح `welcome` فقط |
| `README.md` | مقدمة لعبتي + بقاء README الأصلي كاملًا |
| `NOTICE` (جديد) | نسبة المصدر وإشعار الترخيص |
| `LOBATI_REPORT_AR.md` (جديد) | هذا التقرير |

## ما لم أغيّره عمدًا
- **معرّف الحزمة** `com.star4droid.star2d.evo` (applicationId وnamespace وFileProvider authority وMTDataFilesProvider) — ينتظر قرارك كما طلبت. ملاحظة: القالب `template` له حزمته (`com.star4droid.template`)، والألعاب المصدَّرة تولّد حزمًا فريدة تلقائيًا؛ لا تلمس هذا بدون فحص.
- لا حذف لأي وظيفة: المحرر والأحداث والتصادمات والتصدير كما هي. لم أعدّل `template.zip` ولا `java/manifest.xml` ولا `java/game.zip` ولا `template.version` (مولّدة).
- الأيقونة: لم أغيّرها؛ لا أداة صور مناسبة هنا لإنتاج أيقونة ومعالجتها بأمان (والمصدّر يستبدل `res/drawable/icon.jpg` في القالب، فلا تُغيَّر عشوائيًا).

## أماكن تخص Star2D ما زالت في التطبيق (تحتاج قرارك)
1. **فاحص التحديثات** `Adapters/UpdateChecker.java`: يقرأ `raw.githubusercontent.com/star4droid/Star2D/.../latest.version` ويفتح صفحة releases لـStar2D. أي مستخدم لـ«لعبتي» قد يُدفع لتنزيل تطبيق Star2D (والتطبيق يطلب `REQUEST_INSTALL_PACKAGES`). **أولوية عالية** قبل أي توزيع.
2. **خدمات سحابية/خارجية موجودة أصلًا** رغم قيدك: Firebase Analytics وCrashlytics (تعمل فقط إن وُجد `app/google-services.json`، وهو غير موجود)، ومكتبة Billing وشاشة `DonateActivity` ونافذة الدعم `SupportPrompt`، ومساعد AI (OpenRouter/OpenCode Zen)، وWebSocket للتحكم عن بعد عبر `star2d.ans44029.workers.dev`، وإذن INTERNET، وروابط مجتمع Star2D (فيسبوك، ديسكورد، واتساب، تيليجرام، رقم المطوّر). لم أحذف شيئًا (لا حذف دون موافقتك).
3. نصوص الدعم/المجتمع/المساعد الذكي (مثل `SupportPrompt` و`AiContext` و`CommunityLinks` و`home_support_sub`) ما زالت تذكر Star2D.
4. **مفتاح التوقيع المرفق** `app/Star4Droid.jks`: مفتاح المؤلف الأصلي مرفوع في المستودع. اسم الملف الافتراضي في `build.gradle` هو `Star4droid.jks` (حرف d صغير) فعلى Linux لن يُعثر عليه ويسقط البناء إلى مفتاح debug. **أنشئ مفتاحك الخاص ولا تعتمد على هذا المفتاح** عند أي إصدار.
5. `build-aab.yml` يرفع إلى Google Play و`opencode.yml` يشغّل وكيل AI بمفاتيح أسرار؛ كلاهما يدوي (`workflow_dispatch`) فلا يعملان تلقائيًا، لكن راجعهما قبل تفعيل أي سر.
6. `build-template.yml` يعيد توليد القالب ويدفعه تلقائيًا إلى `master` عند تغيير ملفات الـruntime — مفيد لكنه يتطلب صلاحية كتابة (`contents: write`) في مستودعك.

## الترخيص والإشعارات
- Apache-2.0 (LICENSE سليم). تم الحفاظ عليه، وأُضيف `NOTICE` ونسبة المصدر في README وشاشة التحميل، مع تعليق «Modified for Lo3bati» في الملفات المعدّلة.
- لم أدقق تراخيص المكتبات الخارجية.

## بناء GitHub Actions (من قراءة الملفات، لم يُشغَّل)
`android-build.yml` (يدوي): JDK 21 + NDK r28c ثم `./gradlew clean assembleRelease` ويرفع `app-release.apk`. الإعدادات: AGP 8.13.2، Gradle 9.2.1، compileSdk/targetSdk 36، minSdk 24، Java 21، buildTools 37.0.0.

## الخطوة التالية المقترحة
ارفع المشروع إلى مستودعك، شغّل «Build Apk» وأرسل لي النتيجة أو سجل الأخطاء؛ ثم اختبر على هاتف: تشغيل مثال السيارات وتصدير APK وتثبيته. بعدها نقرر (1) معرّف الحزمة، (2) تعطيل/استبدال فاحص التحديثات، (3) مصير الخدمات الخارجية.

---

# المرحلة التالية: معرّف الحزمة `com.lo3bati.gamecreator`

## ما تغيّر
| الملف | التغيير |
|---|---|
| `app/build.gradle` | `applicationId` ← `com.lo3bati.gamecreator`. **`namespace` بقي `com.star4droid.star2d.evo`** عمدًا (حزمة R وBuildConfig المشتركة مع `:template`). كلمة مرور المفتاح تقرأ `KEY_PASSWORD` اختياريًا ثم تعود إلى `KEYSTORE_PASSWORD` |
| `app/src/main/AndroidManifest.xml` | authority الـFileProvider وauthority وtaskAffinity الخاصة بـMTData صارت `${applicationId}…` (الكود يبني الـauthority من `getPackageName()`) |
| `.github/workflows/build-aab.yml` | `packageName` ← `com.lo3bati.gamecreator` (يُستعمل فقط عند رفع Play) |
| `app/.../EditorActivity.java` | سطر استدعاء `UpdateChecker.checkForUpdate` معطّل بتعليق. الصنف نفسه باقٍ، وروابطه ما زالت تشير إلى Star2D حتى تنشر مستودعك |

## ما لم يتغيّر عمدًا
- `template/**` ومعرّف القالب `com.star4droid.template` (حزمة كل لعبة مصدَّرة يكتبها المستخدم وقت التصدير).
- `app/Star4Droid.jks` الخاص بمؤلف Star2D ما زال في المشروع. احذفه من مستودعك بنفسك (انظر الخطوات).
- Firebase وBilling والمساعد الذكي والاتصال البعيد وبقية الوظائف.

## الاختبار
- نجح: `make_template.py check` (القالب محدّث) و`verify_template.py` وفحص XML للـmanifest.
- **لم يُختبر:** البناء، وتثبيت التطبيق، وFileProvider، وتصدير لعبة، وبناء GitHub Actions. بيئتي بلا Android SDK ولا شبكة.

## خطواتك (واحدة واحدة)
1. أنشئ مستودعًا جديدًا على GitHub وارفع محتوى المشروع.
2. في Actions شغّل **Build Apk** يدويًا، وأرسل لي النتيجة أو سجل الأخطاء.
3. أنشئ keystore خاصًا بك بـ`keytool` وخزّنه خارج المستودع، وأضف الأسرار `KEYSTORE_BASE64` و`KEYSTORE_PASSWORD` و`KEY_ALIAS` عند الحاجة لتوقيع الإصدارات.
4. احذف `app/Star4Droid.jks` من المستودع.
5. ثبّت الـAPK على هاتف وجرّب: فتح المحرر، مشاركة ملف، تصدير لعبة بحزمة تجريبية (مثل `com.test.game`) وتثبيتها.
6. بعد نشر مستودعك، نعدّل روابط `UpdateChecker` ونعيد تفعيل الاستدعاء.

---

# إزالة ارتباطات المؤلف الأصلي

## ما أُزيل أو عُطّل
| الموضع | التغيير |
|---|---|
| `ui/CommunityLinks.java` | حُذفت روابط المجتمع الستة (Facebook وWhatsApp وTelegram وDiscord وGitHub وYouTube) فصارت القائمة فارغة، وفُرّغت محفظة USDT ورقم واتساب وحساب تيليجرام الخاصة بالمؤلف |
| `editor/ui/ProjectsListStage.java` | حُذفت الروابط الستة المفتوحة من الأزرار (الواجهة القديمة) |
| `Views/ProjectsHomeView.java` | زر Donate مخفي بـ`View.GONE`، و`DonateActivity` باقٍ في المشروع |
| `EditorActivity.java` | نافذة الدعم الدورية (SupportPrompt) معطّلة بتعليق، إضافة إلى تعطيل فاحص التحديثات سابقًا |
| `DebugActivity.java` | زر مراسلة المؤلف عبر Messenger في شاشة الأعطال مخفي ومعطّل |
| `Adapters/SPNote.java` | أُزيلت بيانات دفع Payeer الخاصة بالمؤلف ورابط قناته |
| `remote/RemoteSettings.java` | `DEFAULT_SERVER` فارغ بدل خادم المؤلف (الكود مصمم ليتعامل مع الفراغ) |
| `git/GitHubApi.java` | `CLIENT_ID` فارغ؛ ضع معرّف تطبيق OAuth الخاص بك قبل استعمال ربط GitHub |
| `Adapters/UpdateChecker.java` | روابط التحديث فارغة |
| `ui/ai/AiClient.java` | رأس HTTP-Referer فارغ |
| `app/Star4Droid.jks` | حُذف (مفتاح توقيع المؤلف) |
| `assets/privacy-policy.md` | استُبدل بنص مؤقت؛ النص السابق يسمّي المؤلف وبريده |
| `README.md` | استُبدل بنص لعبتي مع نسبة المصدر |

## ما بقي عمدًا (التزامات الترخيص أو تحتاج قرارك)
- `LICENSE` و`NOTICE` وتعليقات حقوق المؤلف داخل ملفات المصدر (مثل `CustomColliderEditor.java`): ترخيص Apache-2.0 يوجب إبقاءها.
- ما زالت كلمة «Star2D» تظهر في مواضع أخرى: نصوص المساعد الذكي (`EditorAI` و`AiContext`)، ودروس التعليم، وملفات الترجمة، وأسماء الحزم البرمجية (`com.star4droid...`). لم أغيّرها لأنها واسعة وتحتاج بناءً واختبارًا.
- الأمثلة الجاهزة في `assets/files/examples` (مثل StarValley وPaul) من أعمال المؤلف، ولم أحذفها.
- `res/values-ru/strings.xml` رابط مترجم روسي (طرف ثالث).
- `DonateActivity` (واجهة Billing) ومعرّفات منتجاتها ما زالت في الكود دون أي منفذ إليها.
- اسم الجهة `star2d_remote` في SharedPreferences ومسارات مثل `Star2D/exports` ما زالت.

## الفحص
- فُحصت صياغة الملفات المعدّلة بـjavac في وضع التحليل: لا أخطاء صياغة، وتوجد أخطاء رموز مفقودة متوقعة لغياب مكتبات Android. أُصلح خطأ كنت قد أدخلته في `EditorActivity` (شرط `if` بلا جسم) قبل التسليم.
- `make_template.py check` يُعاد تشغيله عند التسليم.
- **لم يُبنَ التطبيق ولم يُجرَّب.**

---

# إصلاح فشل البناء (validateSigningRelease)
- **السبب (من السجل ومن `app/build.gradle`):** مفتاح الإصدار `Star4droid.jks` غير موجود، فيعود البناء إلى مفتاح debug، لكن مفتاح debug غير موجود أيضًا على جهاز GitHub النظيف (`/home/runner/.config/.android/debug.keystore`) لأن `assembleRelease` لا ينشئه.
- **الإصلاح:** في `.github/workflows/android-build.yml` أُضيفت خطوة قبل البناء: إن وُجد السر `KEYSTORE_BASE64` تُستعاد منه keystore الإصدار إلى `app/Star4droid.jks`، وإلا يُولَّد مفتاح debug. وتُمرَّر `KEYSTORE_PASSWORD` و`KEY_ALIAS` و`KEY_PASSWORD` من الأسرار.
- **تنبيه:** بدون الأسرار يُوقَّع الـAPK بمفتاح جديد في كل تشغيل، فلا يُحدَّث فوق نسخة سابقة (احذف القديمة قبل التثبيت). للتحديثات المتتالية أنشئ مفتاحك وأضف الأسرار.
- لم يُشغَّل هذا الإصلاح على GitHub بعد.
