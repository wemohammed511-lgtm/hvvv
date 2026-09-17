package com.example.engine

import java.util.Locale
import java.util.regex.Pattern

object OfflineAiEngine {

    data class OfflineResponse(
        val text: String,
        val category: String,
        val confidence: Float = 1.0f
    )

    fun generateResponse(userPrompt: String): OfflineResponse {
        val trimmed = userPrompt.trim()
        val lower = trimmed.lowercase(Locale.ROOT)

        // 1. Identity & Introduction
        if (isIdentityQuery(lower)) {
            return OfflineResponse(
                text = """
                    👋 **أهلاً بك! أنا MS Almohtal**
                    
                    أنا مساعدك الذكي المتطور، ومصمم خصيصاً لمساعدتك في كل ما تحتاج إليه:
                    
                    🔹 **العمل بدون إنترنت (Offline Mode):**
                    أعمل الآن معك مباشرة من داخل جهازك دون الحاجة لأي اتصال بالإنترنت! أستطيع حل المسائل الحسابية، كتابة الأكواد، تقديم أفكار، شرح المفاهيم، وحفظ سجل محادثاتك.
                    
                    🔹 **الوضع السحابي (Cloud API):**
                    عند توفر الإنترنت ووضع مفتاح الـ API، أتحول لقوة الذكاء الاصطناعي السحابية الكاملة (Gemini 3.5 Flash) للإجابة على الأسئلة الحية والمعقدة وتوليد النصوص الطويلة.
                    
                    💡 جرّب أن تسألني عن:
                    • مسألة رياضية (مثل: 25 * 40 أو احسب النسبة المئوية)
                    • كود برمجي (Python, Kotlin, JavaScript)
                    • أفكار لمشاريع جديدة أو صياغة إيميل رسمي
                """.trimIndent(),
                category = "Identity"
            )
        }

        // 2. Offline Question check ("ينفع يشتغل بدون نت؟")
        if (lower.contains("بدون نت") || lower.contains("بدون انترنت") || lower.contains("offline") || lower.contains("من غير نت")) {
            return OfflineResponse(
                text = """
                    ⚡ **نعم، أنا أعمل بدون إنترنت بالفعل!**
                    
                    إليك كيف يعمل نظام **MS Almohtal**:
                    
                    1️⃣ **المحرك المحلي (المفعل الآن):**
                    يمتلك التطبيق نواة ذكاء محلية مدمجة تعالج طلباتك على هاتفك مباشرة:
                    - الحسابات والمعادلات الرياضية
                    - مساعدة برمجية وقوالب كود شائعة
                    - أفكار للمشاريع، التلخيص، وصياغة الرسائل
                    - حفظ واستعراض كامل محادثاتك محلياً في قاعدة بيانات سريعة (Room Database).
                    
                    2️⃣ **محرك السحابة (Cloud API):**
                    نماذج الـ LLM العملاقة (مثل Gemini) تتطلب خوادم سحابية ضخمة. عند اتصالك بالإنترنت وتفعيل مفتاح الـ API، يتم إرسال استفساراتك إلى النموذج السحابي للحصول على أقصى دقة وتفكير عميق.
                    
                    ✨ **النتيجة:** لديك تطبيق هجين يعمل في كل الظروف، سواء كان معك إنترنت أو في وضع الطيران!
                """.trimIndent(),
                category = "OfflineCapability"
            )
        }

        // 3. Math & Calculation Engine
        val mathResult = tryEvaluateMath(trimmed)
        if (mathResult != null) {
            return OfflineResponse(
                text = mathResult,
                category = "Math"
            )
        }

        // 4. Programming & Code Helpers
        val codeResult = tryHandleCode(lower, trimmed)
        if (codeResult != null) {
            return OfflineResponse(
                text = codeResult,
                category = "Coding"
            )
        }

        // 5. Writing, Emails, and Summarization
        val writingResult = tryHandleWriting(lower, trimmed)
        if (writingResult != null) {
            return OfflineResponse(
                text = writingResult,
                category = "Writing"
            )
        }

        // 6. Knowledge Base & General Facts
        val knowledgeResult = tryHandleKnowledge(lower)
        if (knowledgeResult != null) {
            return OfflineResponse(
                text = knowledgeResult,
                category = "Knowledge"
            )
        }

        // 7. General Smart Local Fallback with Guidance
        return OfflineResponse(
            text = """
                💡 **استجابة المحرك الذكي المحلي [MS Almohtal - وضع بدون نت]:**
                
                لقد استلمت استفسارك: *"$trimmed"*
                
                📌 **تحليل أولي:**
                أنا أعمل حالياً في **الوضع المحلي (Offline)** على جهازك مباشرة.
                
                🔹 للتعامل مع هذا الموضوع:
                1. يمكنك تجربة تحديد السؤال بصيغة محددة (مثل: "اكتب كود..." أو "احسب..." أو "اقترح أفكار لـ...").
                2. لحصول على إجابة تحليلية شاملة ومباشرة من الـ AI السحابي، تأكد من الاتصال بالإنترنت، ويمكنك إدخال مفتاح API الخاص بك في الإعدادات أعلى الشاشة ⚙️!
                
                ✨ هل تود أن نناقش جانباً برمجياً، حسابياً، أو فكرة مشروع حول هذا الموضوع؟
            """.trimIndent(),
            category = "GeneralFallback",
            confidence = 0.7f
        )
    }

    private fun isIdentityQuery(q: String): Boolean {
        return q.contains("من انت") || q.contains("مين انت") || q.contains("ما اسمك") ||
                q.contains("who are you") || q.contains("who r u") || q.contains("ms almohtal") ||
                q == "مرحبا" || q == "أهلا" || q == "اهلا" || q == "hello" || q == "hi" || q == "سلام"
    }

    private fun tryEvaluateMath(prompt: String): String? {
        val clean = prompt.replace("×", "*").replace("÷", "/")
        val pattern = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*([+\\-*/%^])\\s*([0-9]+(?:\\.[0-9]+)?)")
        val matcher = pattern.matcher(clean)
        if (matcher.find()) {
            val num1 = matcher.group(1)?.toDoubleOrNull() ?: return null
            val op = matcher.group(2)
            val num2 = matcher.group(3)?.toDoubleOrNull() ?: return null

            val result = when (op) {
                "+" -> num1 + num2
                "-" -> num1 - num2
                "*" -> num1 * num2
                "/" -> if (num2 != 0.0) num1 / num2 else Double.NaN
                "%" -> num1 % num2
                "^" -> Math.pow(num1, num2)
                else -> return null
            }

            val formattedRes = if (result.isNaN()) "غير معرف (القسمة على صفر)" else if (result == result.toLong().toDouble()) result.toLong().toString() else "%.4f".format(Locale.US, result)
            return """
                🧮 **النتيجة الحسابية [محرك MS Almohtal المحلي]:**
                
                العملية: `$num1 $op $num2`
                الناتج: **$formattedRes**
                
                📊 تفاصيل:
                • الطرف الأول: $num1
                • العملية: $op
                • الطرف الثاني: $num2
            """.trimIndent()
        }

        // Percentage check like: "20% من 500" or "احسب نسبة"
        val percentPattern = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*%\\s*(?:من|of)?\\s*([0-9]+(?:\\.[0-9]+)?)")
        val pMatcher = percentPattern.matcher(clean)
        if (pMatcher.find()) {
            val pct = pMatcher.group(1)?.toDoubleOrNull() ?: return null
            val base = pMatcher.group(2)?.toDoubleOrNull() ?: return null
            val value = (pct / 100.0) * base
            return """
                📊 **حساب النسبة المئوية:**
                
                $pct% من $base = **$value**
                
                خطوات الحل:
                ($pct ÷ 100) × $base = $value
            """.trimIndent()
        }

        return null
    }

    private fun tryHandleCode(lower: String, original: String): String? {
        if (lower.contains("python") || lower.contains("بايثون")) {
            return """
                🐍 **كود Python مقترح:**
                
                ```python
                # مثال دالة مفيدة لحل المشكلات وقراءة البيانات
                def process_data(items):
                    results = []
                    for item in items:
                        if item:
                            results.append(str(item).strip().title())
                    return results

                # تجربة الدالة
                data = ["ms", "almohtal", "ai", "assistant"]
                print("النتيجة:", process_data(data))
                ```
                💡 *نصيحة:* يمكنك تشغيل الكود في أي بيئة Python 3.
            """.trimIndent()
        }

        if (lower.contains("kotlin") || lower.contains("كوتلن") || lower.contains("اندرويد") || lower.contains("android")) {
            return """
                🟣 **كود Kotlin (Android Jetpack Compose):**
                
                ```kotlin
                @Composable
                fun CustomCard(title: String, description: String) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = title, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = description, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                ```
            """.trimIndent()
        }

        if (lower.contains("javascript") || lower.contains("js") || lower.contains("جافا سكريبت")) {
            return """
                ⚡ **كود JavaScript حديث (ES6+):**
                
                ```javascript
                // جلب بيانات من API خارجي بشكل غير متزامن
                async function fetchAiData(endpoint) {
                  try {
                    const response = await fetch(endpoint);
                    if (!response.ok) throw new Error("فشل الاتصال");
                    const data = await response.json();
                    return data;
                  } catch (error) {
                    console.error("خطأ:", error);
                    return null;
                  }
                }
                ```
            """.trimIndent()
        }

        if (lower.contains("sql") || lower.contains("قاعدة بيانات") || lower.contains("database")) {
            return """
                🗄️ **استعلام SQL لإنشاء جدول المحادثات:**
                
                ```sql
                CREATE TABLE chat_messages (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sender TEXT NOT NULL,
                    content TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    is_offline INTEGER DEFAULT 0
                );

                -- استرجاع أحدث الرسائل
                SELECT * FROM chat_messages 
                ORDER BY timestamp DESC 
                LIMIT 20;
                ```
            """.trimIndent()
        }

        if (lower.contains("كود") || lower.contains("برمجة") || lower.contains("code")) {
            return """
                💻 **المساعد البرمجي المحلي لـ MS Almohtal:**
                
                أنا جاهز لمساعدتك في كتابة وشرح الأكواد في مختلف اللغات:
                • **Python**: معالجة البيانات، الذكاء الاصطناعي، الأتمتة
                • **Kotlin / Android**: Jetpack Compose، Room Database، Coroutines
                • **JavaScript / TypeScript**: تطبيقات الويب وتفاعل الـ UI
                • **SQL**: قواعد البيانات والاستعلامات
                
                حدد لي المطلوب أو اللغة التي تود البدء بها!
            """.trimIndent()
        }

        return null
    }

    private fun tryHandleWriting(lower: String, original: String): String? {
        if (lower.contains("ايميل") || lower.contains("رسالة") || lower.contains("email") || lower.contains("خطاب")) {
            return """
                ✉️ **نموذج بريد إلكتروني رسمي مقترح:**
                
                **الموضوع:** استفسار بخصوص [الموضوع المطلوب]
                
                السيد/السيدة المحترم/ة،
                
                تحية طيبة وبعد،،
                
                أكتب إليكم هذه الرسالة للتواصل بخصوص [أدخل النقطة الرئيسية هنا]. أود التعبير عن اهتمامي بمتابعة هذا الأمر، وأكون ممتناً لو تم التكرم بمشاركتي التفاصيل في أقرب وقت يناسبكم.
                
                شاكراً لكم حسن تعاونكم واهتمامكم.
                
                وتفضلوا بقبول فائق الاحترام والتقدير،
                [اسمك]
                [وسيلة التواصل]
            """.trimIndent()
        }

        if (lower.contains("فكرة") || lower.contains("مشروع") || lower.contains("ideas") || lower.contains("project")) {
            return """
                🚀 **أفكار مشاريع ذكية واعدة:**
                
                1️⃣ **تطبيق إدارة المهام الذكي بنظام الأوفلاين**:
                يساعد المستخدمين على تنظيم يومهم دون الحاجة لإنترنت مع مزامنة سحابية عند توفر الاتصال.
                
                2️⃣ **منصة مساعدة برمجية للطلاب**:
                مساعد ذكي يشرح المفاهيم الخوارزمية خطوة بخطوة باللغة العربية مع أمثلة مرئية.
                
                3️⃣ **بوت محلي للتلخيص وحفظ الملاحظات**:
                يعتمد على قواعد البيانات المحلية لحفظ الملاحظات واستخراج النقاط الهامة فورياً.
                
                💡 *أي من هذه الأفكار ترغب في تفصيل خطة عملها؟*
            """.trimIndent()
        }

        if (lower.contains("لخص") || lower.contains("تلخيص") || lower.contains("summarize")) {
            return """
                📝 **طريقة التلخيص الفعالة [MS Almohtal]:**
                
                لتلخيص أي نص بدقة، قسّمه إلى 3 عناصر:
                1. **الفكرة المحورية:** ما هو الهدف الرئيسي من النص؟
                2. **النقاط الداعمة:** 3 إلى 5 نقاط تشرح كيفية تحقيق هذا الهدف.
                3. **الخلاصة والقرار:** ما هي النتيجة النهائية أو الخطوة التالية؟
                
                💡 *الصق النص هنا وسأقوم بمساعدتك في تفكيكه واستخراج خلاصته!*
            """.trimIndent()
        }

        return null
    }

    private fun tryHandleKnowledge(lower: String): String? {
        if (lower.contains("الذكاء الاصطناعي") || lower.contains("ai")) {
            return """
                🤖 **ما هو الذكاء الاصطناعي (AI)؟**
                
                هو فرع من فروع علوم الحاسوب يهدف إلى بناء أنظمة قادرة على محاكاة القدرات الذهنية البشرية، مثل:
                • **التعلم (Machine Learning):** استخلاص الأنماط من البيانات.
                • **معالجة اللغة الطبيعية (NLP):** فهم النصوص والمحادثة مثلما نفعل الآن.
                • **النماذج التوليدية (Generative AI):** ابتكار نصوص، صور، وأكواد جديدة كلياً.
            """.trimIndent()
        }

        if (lower.contains("سرعة الضوء")) {
            return "⚡ **سرعة الضوء** في الفراغ تبلغ حوالي **299,792 كيلومتر في الثانية** (تقريباً 300 ألف كم/ث)، وهي أقصى سرعة يمكن أن تسافر بها المعلومات في الكون وفقاً للنظرية النسبية."
        }

        if (lower.contains("الكواكب") || lower.contains("المجموعة الشمسية")) {
            return "🪐 **كواكب المجموعة الشمسية الثمانية** بترتيب بعدها عن الشمس: عطارد، الزهرة، الأرض، المريخ، المشتري، زحل، أورانوس، ونبتون."
        }

        return null
    }
}
