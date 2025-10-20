package com.newAi302.app.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.newAi302.app.R
import com.newAi302.app.base.BaseActivity
import com.newAi302.app.databinding.ActivityAnnouncementBinding
import com.newAi302.app.databinding.ActivityProtocolBinding
import com.newAi302.app.databinding.ActivityVersionInformationBinding
import com.newAi302.app.utils.LanguageUtil

class ProtocolActivity : BaseActivity() {
    private lateinit var binding: ActivityProtocolBinding
    private var defaultSystemLanguage = "zh"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProtocolBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backImage.setOnClickListener {
            finish()
        }

        /*binding.webViewUrl.settings.javaScriptEnabled = true// 启用JavaScript（可选，根据HTML内容需求）
        binding.webViewUrl.settings.domStorageEnabled = true// 启用DOM存储（可选）

        binding.webViewUrl.loadUrl(
            "https://302.ai/legal/terms/"
        )*/

        binding.useCons.setOnClickListener {
            showBottomPayProtocolDialog(this@ProtocolActivity,true)
        }
        binding.privacyCons.setOnClickListener {
            showBottomPayProtocolDialog(this@ProtocolActivity,false)
        }


        binding.webViewUrl.apply {
            // 设置背景色为白色（覆盖默认的透明/黑色）
            setBackgroundColor(Color.WHITE)
            // 加载前显示白色背景（避免短暂黑屏）
            background = ContextCompat.getDrawable(context, android.R.color.white)

            // 其他已有配置
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            // 1. 支持 TLS 1.0~1.3（适配低版本 Android）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW // 允许混合内容（HTTPS 加载 HTTP 资源）
                webViewClient = object : WebViewClient() {
                    // 解决 TLS 协议支持问题
                    override fun onReceivedSslError(
                        view: WebView?,
                        handler: SslErrorHandler?,
                        error: SslError?
                    ) {
                        // 注意：仅调试时使用！正式环境需验证证书，避免安全风险
                        handler?.proceed() // 忽略 SSL 证书错误（临时排查用）
                    }

                    // 捕获加载错误，打印日志定位问题
                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        // 打印错误信息（在 Logcat 中搜索 "WebViewError"）
                        Log.e("WebViewError", "错误码：${error?.errorCode}，描述：${error?.description}")
                    }
                }
            }

            // 2. 设置电脑浏览器的 User-Agent（绕过服务器设备过滤）
            val pcUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            settings.userAgentString = pcUserAgent

            // 新增：屏幕适配核心设置
            settings.apply {
                // 1. 让WebView支持<meta name="viewport">标签
                useWideViewPort = true  // 允许网页的viewport设置生效
                //loadWithOverviewMode = true  // 缩放至屏幕大小

                // 2. 设置初始缩放比例（0表示不缩放，自动适配）
                //setInitialScale(0)

                // 3. 可选：允许用户手动缩放（根据需求开启）
                builtInZoomControls = false  // 关闭内置缩放控件
                supportZoom() // 禁用缩放功能（避免用户手动缩放导致布局错乱）

                // 4. 适配不同屏幕密度
                displayZoomControls = false  // 隐藏缩放按钮
            }

            // 可选：如果网页本身是黑色背景，强制注入 CSS 修改
//            webViewClient = object : WebViewClient() {
//                override fun onPageFinished(view: WebView?, url: String?) {
//                    super.onPageFinished(view, url)
//                    // 注入 JS 强制网页背景为白色（针对网页自身样式问题）
//                    view?.loadUrl("javascript:(function() { " +
//                            "document.body.style.backgroundColor = '#ffffff'; " +
//                            "document.body.style.color = '#000000'; " +  // 同时确保文字颜色为黑色
//                            "})()")
//                }
//            }

            loadUrl("https://302.ai/legal/privacy/")
        }
    }

    override fun onResume() {
        super.onResume()
        val language = LanguageUtil.getSavedLanguage(this)
        Log.e("ceshi","获取的语言是:$language")
        defaultSystemLanguage = language

    }


    @SuppressLint("MissingInflatedId")
    private fun showBottomPayProtocolDialog(context: Context, isUse:Boolean){
        // 使用 requireContext() 获取正确的 Context
        val bottomSheetDialog = BottomSheetDialog(context)

        // 为 BottomSheetDialog 设置布局
        val view: View =  LayoutInflater.from(context).inflate(R.layout.bottom_sheet_pay_some_layout, null)
        bottomSheetDialog.setContentView(view)

        // 获取BottomSheetBehavior
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet!!)

        val payProtocolWeb = view.findViewById<WebView>(R.id.payProtocolWeb)
        val titleProtocolTv = view.findViewById<TextView>(R.id.titleProtocolTv)
        titleProtocolTv.text = ""
        val payDialogSomeBackLine = view.findViewById<LinearLayout>(R.id.payDialogSomeBackLine)
        if (isUse){
            when (defaultSystemLanguage) {
                LanguageUtil.LANGUAGE_ZH -> payProtocolWeb.loadDataWithBaseURL(null, useMsg, "text/html", "utf-8", null)
                LanguageUtil.LANGUAGE_JA -> payProtocolWeb.loadDataWithBaseURL(null, useMsgJa, "text/html", "utf-8", null)
                else -> payProtocolWeb.loadDataWithBaseURL(null, useMsgEn, "text/html", "utf-8", null)
            }
        }else{
            when (defaultSystemLanguage) {
                LanguageUtil.LANGUAGE_ZH -> payProtocolWeb.loadDataWithBaseURL(null, privacyMsg, "text/html", "utf-8", null)
                LanguageUtil.LANGUAGE_JA -> payProtocolWeb.loadDataWithBaseURL(null, privacyMsgJa, "text/html", "utf-8", null)
                else -> payProtocolWeb.loadDataWithBaseURL(null, privacyMsgEn, "text/html", "utf-8", null)
            }
        }




        payDialogSomeBackLine.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        // 设置为展开状态
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        // 设置最大高度（可选，根据需要调整）
        behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels

        // 显示 BottomSheetDialog
        bottomSheetDialog.show()
    }

    val privacyMsg = "<!DOCTYPE html>\n" +
            "<html lang=\"zh-CN\">\n" +
            "\n" +
            "<head>\n" +
            "  <meta charset=\"UTF-8\">\n" +
            "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "  <title>隐私政策</title>\n" +
            "  <style>\n" +
            "    body {\n" +
            "      font-family: \"Microsoft YaHei\", sans-serif;\n" +
            "      line-height: 1.6;\n" +
            "      margin: 0 auto;\n" +
            "      max-width: 800px;\n" +
            "      padding: 20px;\n" +
            "    }\n" +
            "\n" +
            "    h1,\n" +
            "    h2 {\n" +
            "      text-align: center;\n" +
            "    }\n" +
            "\n" +
            "    h3 {\n" +
            "      margin-top: 20px;\n" +
            "    }\n" +
            "\n" +
            "    p {\n" +
            "      margin: 10px 0;\n" +
            "    }\n" +
            "\n" +
            "    ul {\n" +
            "      margin: 10px 0 10px 20px;\n" +
            "    }\n" +
            "\n" +
            "    li {\n" +
            "      margin-bottom: 8px;\n" +
            "    }\n" +
            "  </style>\n" +
            "</head>\n" +
            "\n" +
            "<body>\n" +
            "  <h1>隐私政策</h1>\n" +
            "  <p style=\"text-align: center;\">2022年9月22日</p>\n" +
            "\n" +
            "  <p>302.AI从事数据业务，因此我们十分重视数据隐私。为客户及企业提供干净、合规和合乎道德的数据，同时保持最高的隐私标准。我们的产品和服务在全球范围内运作，因此我们会持续监控动态的全球隐私环境并做出相应调整，以确保完全遵守隐私法。本隐私政策既适用于积极选择使用我们服务的用户（如下所述），也适用于我们可能在运营过程中处理其个人数据的个人。</p>\n" +
            "\n" +
            "  <p>随时阅读我们的隐私政策的详细分类，或就我们的政策或数据法规合规性有任何问题与我们联系。</p>\n" +
            "\n" +
            "  <p>302.AI（\"302.AI\"、\"我们\"或\"我们\"）是网站302.AI（\"本网站\"）。我们提供的任何随附服务、功能、内容和应用程序（在此统称为\"服务\"）。我们关注您的隐私，并设计了本隐私政策，以帮助您决定是否使用服务并提供有关我们隐私惯例的所有必需信息。如果适用于您，通过访问和使用服务，或以其他格式向我们提供信息，即表示您同意并接受不时修订的本隐私政策的条款，并同意以本隐私政策中规定的方式收集和使用信息。我们鼓励您查看本隐私政策仔细并定期参考它，以便您理解它以及对其所做的任何后续更改。如果您不同意本隐私政策的条款，请立即停止使用服务并在相关的地方删除。</p>\n" +
            "\n" +
            "  <h2  style=\"text-align: left;\">我们收集哪些类型的信息？</h2>\n" +
            "  <p>当您使用服务时，我们会从您那里收集以下类型的数据（\"用户数据\"）：</p>\n" +
            "  <ul>\n" +
            "    <li>非个人信息。这是由用户的活动生成的无法识别和不可识别的信息。此类非个人信息可能包括以下信息 - 浏览器类型、您访问的网页、在这些页面上花费的时间、访问时间和日期。</li>\n" +
            "    <li>个人信息。个人信息是识别您或可能识别您的信息。我们收集和保留的个人信息包括您的IP地址、您的姓名和电子邮件地址、付款和账单信息或我们可能不定时要求的其他信息入职流程和服务提供所需的时间。当您在服务上创建帐户时，您可以使用您在指定第三方网站或服务上的凭据（\"第三方帐户\"）例如Gmail®。这样做将使您能够链接您的帐户和您的第三方帐户。如果您选择此选项，将出现一个第三方帐户弹出框，您需要批准才能继续，并且这将描述我们将获得的信息类型。此信息包括存储在您的第三方帐户中的您的个人信息，例如用户名、电子邮件地址、个人资料图片。任何专门连接的匿名信息或链接到任何个人信息，只要存在这种联系或链接，我们就会将其视为个人信息。</li>\n" +
            "    <li>通过社交网络帐户注册：当您通过您的社交网络帐户（例如Facebook、Google+）注册或登录服务时，我们将可以访问您的社交网络帐户的基本信息，例如您的电子邮件地址，以及您在该帐户上公开或同意与我们分享的任何其他信息。在任何时候，我们都会遵守社交网络平台的条款、条件和限制。</li>\n" +
            "  </ul>\n" +
            "  <p>如果我们将非个人信息与个人信息相结合，我们会将合并后的信息视为个人信息。我们还可能从各种在线来源收集公开发布的个人数据。在大多数情况下，此类信息将包括基本联系信息，例如姓名、电子邮件（\"公共数据\"）。</p>\n" +
            "\n" +
            "  <h2  style=\"text-align: left;\">处理的法律依据</h2>\n" +
            "  <p>处理用户数据对于履行我们对您的合同义务和为您提供我们的服务、保护我们的合法利益和遵守我们的法律义务是必要的。</p>\n" +
            "  <p>公共数据的处理是根据我们的合法利益进行的，只要这种利益不会凌驾于您的基本权利和自由之上。</p>\n" +
            "\n" +
            "  <h2  style=\"text-align: left;\">我们如何使用您的信息？</h2>\n" +
            "  <p>我们使用用户数据是为了向您提供服务并遵守我们的法律要求和内部准则。这意味着我们将使用这些信息来设置您的帐户，为您提供有关服务的支持，与您沟通更新、营销优惠或您可能有的疑虑，并进行统计和分析研究以改进服务。我们可能会使用公共数据为我们的用户提供我们的服务。</p>\n" +
            "\n" +
            "  <h2  style=\"text-align: left;\">我们分享的信息</h2>\n" +
            "  <p>我们不出租或出售任何用户数据。我们可能会与我们的用户共享公共数据以提供我们的服务。我们可能会向其他受信任的第三方服务提供商或合作伙伴披露个人信息，以便为您提供服务、存储和分析，并遵守我们的法律要求和内部准则。我们也可能将个人信息转移或披露给我们的子公司和关联公司。</p>\n" +
            "  <p>尽管有上述规定，我们不会将您的IP地址透露给其他客户。但是，当其他客户（例如网络中的其他对等方）使用您的IP地址访问网络时，他们可能会看到IP地址，例如通过查找他们当前的IP地址。</p>\n" +
            "  <p>如果我们有充分理由认为有必要，我们也可能在特殊情况下共享您的个人信息和其他信息：(1) 遵守法律、法规、传票或法院命令。</p>\n" +
            "  <p>检测、预防或以其他方式解决欺诈、安全、违反我们的政策或技术问题。执行本隐私政策的规定或您与我们之间的任何其他协议，包括调查潜在的违规行为。保护我们、我们的合作伙伴、我们的关联公司、用户或公众的权利、财产或安全免受损害。</p>\n" +
            "\n" +
            "  <h2  style=\"text-align: left;\">安全和保密</h2>\n" +
            "  <p>我们使用行业标准的信息、安全工具和措施，以及内部程序和严格的准则来防止信息滥用和数据泄露。我们的员工有保密义务。我们使用的措施和程序可以大大降低数据滥用的风险，但我们不能保证我们的系统绝对安全。如果您发现任何潜在的数据泄露或安全漏洞，请立即与我们联系。我们将根据需要采取一切措施调查事件，包括预防措施。</p>\n" +
            "\n" +
            "  <h2  style=\"text-align: left;\">您的选择和权利</h2>\n" +
            "  <p>我们努力为您提供快速更新或删除信息的方法——除非我们必须出于合法业务或法律目的保留该信息。在更新您的个人信息时，我们可能会要求您验证您的身份，然后我们才能根据您的请求采取行动。</p>\n" +
            "  <p>请注意以下权利特别适用于您的个人信息：</p>\n" +
            "  <ul>\n" +
            "    <li>收到关于您的个人信息是否正在处理的确认，并访问您存储的个人信息以及补充信息信息。</li>\n" +
            "    <li>以结构化、常用和机器可读的格式接收您直接自愿提供给我们的个人信息的副本。</li>\n" +
            "    <li>要求更正我们您的个人信息。</li>\n" +
            "    <li>要求删除您的个人信息。</li>\n" +
            "    <li>反对我们处理个人信息。</li>\n" +
            "    <li>要求限制我们处理您的个人信息。</li>\n" +
            "    <li>向监管机构提出投诉。</li>\n" +
            "  </ul>\n" +
            "  <p>但是，请注意，这些权利不是绝对的，可能受制于我们自己的合法利益和监管要求。要行使该权利，您可以通过以下方式联系我们：luna@302.AI</p>\n" +
            "\n" +
            "  <h2  style=\"text-align: left;\">保留</h2>\n" +
            "  <p>我们将在提供服务所需的时间内保留您的个人信息，并根据需要遵守我们的法律义务、解决争议和执行我们的政策。保留期限应根据所收集信息的类型和收集目的确定，同时考虑到适用于情况的要求以及尽早销毁过时、未使用的信息的必要性。我们可以随时自行决定更正、补充或删除不完整或不准确的信息。</p>\n" +
            "\n" +
            "  <h2  style=\"text-align: left;\">信息保证免责声明</h2>\n" +
            "  <p>尽管本政策中另有规定，我们不对我们收集、存储和披露给您或其他任何人的任何信息的准确性、正确性和安全性负责。我们的政策仅涉及我们从您那里收集的信息的使用和披露。如果您在整个互联网上向其他方或网站披露您的信息，则不同的规则可能适用于他们使用或披露您向他们披露的信息。因此，我们鼓励您阅读您选择向其披露信息的每个第三方的条款和条件以及隐私政策。</p>\n" +
            "\n" +
            "  <h2  style=\"text-align: left;\">所有权变更</h2>\n" +
            "  <p>如果302.AI的全部或部分所有权或控制权发生变化，包括但不限于通过收购、合并或出售，我们保留转让我们存储在我们的所有或部分用户数据和公共数据的权利系统。您承认，如果发生破产、资不抵债或破产清算，我们可能无法控制您的个人信息的使用和转移。</p>\n" +
            "\n" +
            "  <h2  style=\"text-align: left;\">本隐私政策的变更</h2>\n" +
            "  <p>我们可能会自行决定不定时修订本政策，因此请定期查看。上次修订将反映在\"上次更新\"标题中。本政策的任何更改都将发布在网站上。您在任何此类更改后继续使用服务将被视为您同意修订后的隐私政策。</p>\n" +
            "\n" +
            "  <h2  style=\"text-align: left;\">联系我们</h2>\n" +
            "  <p>如果您认为您的隐私没有按照我们的隐私政策得到处理，或者如果您认为您的隐私在使用服务的过程中受到任何人的损害，请通过以下方式联系302.AI：luna@302.AI和我们的隐私官员应及时调查。用户权利</p>\n" +
            "\n" +
            "  <h2  style=\"text-align: left;\">访问个人信息</h2>\n" +
            "  <p>您每年最多可以要求我们向您披露我们收集的有关您的个人信息的类别和具体部分、收集您的个人信息的来源类别、收集的业务（如果适用）。删除请求 您有权要求我们删除从您那里收集并保留的任何个人信息，除非有例外情况。一旦我们收到并确认您的可验证消费者请求，我们将删除（并指示我们的服务提供商、分包商和顾问删除）您的个人信息，除非有例外情况。</p>\n" +
            "\n" +
            "  <h2  style=\"text-align: left;\">行使您的权利</h2>\n" +
            "  <p>您可以通过向我们的电子邮件地址提交可验证的消费者请求来行使您的权利（例如访问和删除）：luna@302.AI，只有您可以提出与您的个人信息相关的消费者请求。请求必须：</p>\n" +
            "  <ul>\n" +
            "    <li>提供足够的信息，以使我们能够合理地验证您是本人或授权代表。</li>\n" +
            "    <li>用足够的细节描述您的请求，以便我们正确理解、评估和回应它。</li>\n" +
            "    <li>如果我们无法验证您的身份或提出请求的权限并确认个人信息与您相关，我们将无法响应您的请求或向您提供个人信息。提出可验证的消费者请求不需要您在我们这里创建帐户。我们只会使用可验证的消费者请求中提供的个人信息来验证请求者的身份或提出请求的权限。我们只会使用可验证的消费者请求中提供的个人信息来验证请求者的身份或提出请求的权限。</li>\n" +
            "  </ul>\n" +
            "\n" +
            "  <h2  style=\"text-align: left;\">响应时间和格式</h2>\n" +
            "  <p>我们的目标是在收到可验证的消费者请求后45天内回复。如果我们需要更多时间，我们将在前45天内以书面形式通知您原因和延期期限。我们将根据您的选择通过邮件或电子方式提供我们的书面回复。我们提供的任何披露将仅涵盖请求之前的12个月期间。在合理可能的情况下，我们将以易于使用的格式提供您的个人信息，并应允许您无障碍地传输信息。我们不会收取费用用来处理或响应您可验证的消费者请求，除非它是过度的、重复的或明显没有根据的。如果我们确定该请求需要付费，我们将告诉您我们做出该决定的原因，并在完成您的请求之前为您提供成本估算。如果被拒绝，我们提供的回复将解释我们无法满足您的要求的原因。请注意，这些权利不是绝对的，请求受任何适用法律要求的约束，包括法律和道德报告或文件保留义务。</p>\n" +
            "\n" +
            "  <h2  style=\"text-align: left;\">不歧视</h2>\n" +
            "  <p>如果您作为消费者行使任何隐私权，我们不会歧视您。我们不会：</p>\n" +
            "  <ul>\n" +
            "    <li>拒绝您提供商品或服务。</li>\n" +
            "    <li>向您收取不同的商品或服务价格或费率，包括通过给予折扣或其他好处，或施加罚款。</li>\n" +
            "    <li>为您提供不同级别或质量的商品或服务。</li>\n" +
            "    <li>建议您可能会收到不同的商品或服务价格或费率或不同级别或质量的商品或服务。</li>\n" +
            "  </ul>\n" +
            "</body>\n" +
            "\n" +
            "</html>\n".trimIndent()
    val privacyMsgEn = "<!DOCTYPE html>\n" +
            "<html lang=\"zh-CN\">\n" +
            "\n" +
            "<head>\n" +
            "  <meta charset=\"UTF-8\">\n" +
            "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "  <title>Privacy Policy</title>\n" +
            "  <style>\n" +
            "    body {\n" +
            "      font-family: \"Microsoft YaHei\", sans-serif;\n" +
            "      line-height: 1.6;\n" +
            "      margin: 0 auto;\n" +
            "      max-width: 800px;\n" +
            "      padding: 20px;\n" +
            "    }\n" +
            "\n" +
            "    h1,\n" +
            "    h2 {\n" +
            "      text-align: center;\n" +
            "    }\n" +
            "\n" +
            "    h3 {\n" +
            "      margin-top: 20px;\n" +
            "    }\n" +
            "\n" +
            "    p {\n" +
            "      margin: 10px 0;\n" +
            "    }\n" +
            "\n" +
            "    ul {\n" +
            "      margin: 10px 0 10px 20px;\n" +
            "    }\n" +
            "\n" +
            "    li {\n" +
            "      margin-bottom: 8px;\n" +
            "    }\n" +
            "  </style>\n" +
            "</head>\n" +
            "\n" +
            "<body>\n" +
            "  <h1>Privacy Policy</h1>\n" +
            "  <p style=\"text-align: center;\">2022年9月22日</p>\n" +
            "\n" +
            " <p>302.AI is in the data business, so we take data privacy very seriously. We provide clean, compliant, and ethical data to our customers and businesses while maintaining the highest privacy standards. Our products and services operate globally, so we continuously monitor the dynamic global privacy environment and make corresponding adjustments to ensure full compliance with privacy laws. This Privacy Policy applies both to users who actively choose to use our services (as described below) and to individuals whose personal data we may process in the course of our operations. </p>\n" +
            "\n" +
            " <p>Read the detailed breakdown of our privacy policy at any time, or contact us with any questions about our policies or data regulatory compliance. </p>\n" +
            "\n" +
            " <p>302.AI (‘302.AI, 'we', or 'us') is the website 302.AI (the 'Site'). We offer any accompanying services, features, content, and applications (collectively, the 'Services'). We care about your privacy and have designed this Privacy Policy to help you decide whether to use the Services and provide all necessary information about our privacy practices. By accessing and using the Services, or providing us with information in other formats, as applicable to you, you agree to and accept the terms of this Privacy Policy, as amended from time to time, and consent to the collection and use of information in the manner set forth in this Privacy Policy. We encourage you to review this Privacy Policy carefully and refer to it regularly so that you understand it and any subsequent changes made to it. If you do not agree to the terms of this Privacy Policy, please immediately stop using the Services and delete the relevant information. </p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">What types of information do we collect? </h2>\n" +
    " <p>When you use the Services, we collect the following types of data from you (\"User Data\"):</p>\n" +
    " <ul>\n" +
    " <li>Non-Personal Information. This is non-identifiable and non-identifiable information generated by a user's activities. Such non-Personal Information may include information such as - browser type, the web pages you visit, the time spent on those pages, and visit times and dates.</li>\n" +
    " <li>Personal Information. Personal Information is information that identifies you or could potentially identify you. The Personal Information we collect and retain includes your IP address, your name and email address, payment and billing information, or other information we may request from time to time as part of the onboarding process and service provision. When you create an account on the Service, you may use your credentials from designated third-party websites or services ('Third-Party Accounts'), such as Gmail®. Doing so will allow you to link your account with your Third-Party Account. If you select this option, a Third-Party Account pop-up box will appear, requiring your approval before proceeding, and will describe the type of information we will obtain. This information includes your Personal Information stored in your Third-Party Account, such as your username, email address, and profile picture. We will treat any Anonymous Information that is specifically linked or linked to any Personal Information as Personal Information, as long as such connection or link exists. </li>\n" +
    " <li>Registering through a Social Network Account: When you register or log in to the Service through your social network account (e.g., Facebook, Google+), we will have access to basic information about your social network account, such as your email address, and any other information you have made public on that account or have agreed to share with us. At all times, we will adhere to the terms, conditions, and restrictions of the social network platform. </li>\n" +
    " </ul>\n" +
    " <p>If we combine non-personal information with personal information, we will treat the combined information as personal information. We may also collect publicly available personal data from various online sources. In most cases, this information will include basic contact information such as name, email (\"Public Data\"). </p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">Legal Basis for Processing</h2>\n" +
    " <p>Processing of user data is necessary to perform our contractual obligations to you and provide you with our services, to protect our legitimate interests, and to comply with our legal obligations. </p>\n" +
    " <p>The processing of Public Data is based on our legitimate interests, as long as such interests do not override your fundamental rights and freedoms. </p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">How do we use your information? </h2>\n" +
    " <p>We use user data to provide you with the Services and to comply with our legal requirements and internal guidelines. This means we will use the information to set up your account, provide you with support regarding the Services, communicate with you about updates, marketing offers, or any concerns you may have, and conduct statistical and analytical research to improve the Services. We may use public data to provide our Services to our users. </p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">Information We Share</h2>\n" +
    " <p>We do not rent or sell any user data. We may share public data with our users to provide our Services. We may disclose personal information to other trusted third-party service providers or partners for the purpose of providing you with the Services, storage, and analysis, and to comply with our legal requirements and internal guidelines. We may also transfer or disclose personal information to our subsidiaries and affiliates. </p>\n" +
    " <p>Notwithstanding the foregoing, we will not disclose your IP address to other customers. However, when other customers (such as other peers in the network) access the network using your IP address, they may see the IP address, for example by looking up their current IP address. </p>\n" +
    " <p>We may also share your Personal Information and Other Information in special circumstances if we have good faith belief that it is necessary to: (1) Comply with the law, regulation, subpoena, or court order. </p>\n" +
    " <p>Detect, prevent, or otherwise address fraud, security, violations of our policies, or technical issues. Enforce the provisions of this Privacy Policy or any other agreement between you and us, including investigation of potential violations. Protect against harm to the rights, property, or safety of us, our partners, our affiliates, our users, or the public. </p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">Security and Confidentiality</h2>\n" +
    " <p>We use industry-standard information, security tools and measures, as well as internal procedures and strict guidelines to prevent information misuse and data breaches. Our employees are subject to confidentiality obligations. The measures and procedures we use can significantly reduce the risk of data misuse, but we cannot guarantee the absolute security of our systems. If you become aware of any potential data breach or security vulnerability, please contact us immediately. We will take all necessary steps to investigate the incident, including preventive measures. </p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">Your Choices and Rights</h2>\n" +
    " <p>We strive to provide you with ways to quickly update or delete your information - unless we must retain that information for legitimate business or legal purposes. When updating your personal information, we may ask you to verify your identity before we can act on your request. </p>\n" +
    " <p>Please note that the following rights apply specifically to your personal information: </p>\n" +
    " <ul>\n" +
    " <li>Receive confirmation that your personal information is being processed and access your stored personal information and supplement that information. </li>\n" +

    " <li>Receive a copy of the personal information you directly and voluntarily provide to us in a structured, commonly used, and machine-readable format. </li>\n" +
    " <li>Request correction of your personal information from us. </li>\n" +
    " <li>Request erasure of your personal information. </li>\n" +
    " <li>Object to our processing of your personal information. </li>\n" +
    " <li>Request restriction of our processing of your personal information. </li>\n" +
    " <li>Lodge a complaint with a supervisory authority. </li>\n" +
    " </ul>\n" +
    " <p>However, please note that these rights are not absolute and may be subject to our own legitimate interests and regulatory requirements. To exercise this right, you may contact us at luna@302.AI</p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">Retain</h2>\n" +
    " <p>We will retain your personal information for as long as necessary to provide the Services and as needed to comply with our legal obligations, resolve disputes, and enforce our policies. Retention periods are determined based on the type of information collected and the purpose for which it was collected, taking into account the requirements applicable to the circumstances and the need to destroy outdated, unused information as soon as possible. We may correct, supplement, or delete incomplete or inaccurate information at any time, at our sole discretion. </p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">Information Warranty Disclaimer</h2>\n" +
    " <p>Notwithstanding anything else in this Policy, we are not responsible for the accuracy, correctness, or security of any information we collect, store, and disclose to you or anyone else. Our Policy only addresses the use and disclosure of information we collect from you. If you disclose your information to other parties or websites across the internet, different rules may apply to their use or disclosure of the information you disclose to them. Therefore, we encourage you to read the terms and conditions and privacy policies of each third party to which you choose to disclose information. </p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">Information Warranty Disclaimer</h2>\n" +
    " <p>Notwithstanding anything else in this Policy, we are not responsible for the accuracy, correctness, or security of any information we collect, store, and disclose to you or anyone else. Our Policy only addresses the use and disclosure of information we collect from you. If you disclose your information to other parties or websites across the internet, different rules may apply to their use or disclosure of the information you disclose to them. Therefore, we encourage you to read the terms and conditions and privacy policies of each third party to which you choose to disclose information. left;\">Change of Ownership</h2>\n" +
    " <p>If the ownership or control of all or part of 302.AI changes, including but not limited to through acquisition, merger, or sale, we reserve the right to transfer all or part of the user data and public data we store on our systems. You acknowledge that in the event of bankruptcy, insolvency, or liquidation, we may not be able to control the use and transfer of your personal information.</p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">Changes to This Privacy Policy</h2>\n" +
    " <p>We may revise this Policy from time to time at our sole discretion, so please review it periodically. The last revision will be reflected in the \"Last Updated\" heading. Any changes to this Policy will be posted on the Site. Your continued use of the Services after any such changes will be deemed your agreement to the revised Privacy Policy.</p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">Contact Us</h2>\n" +
    " <p>If you believe your privacy has not been handled in accordance with our Privacy Policy, or if you believe your privacy has been compromised by anyone while using the Services, please contact 302.AI at luna@302.AI and our Privacy Officer will promptly investigate. User Rights</p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">Access Personal Information</h2>\n" +
    " <p>Up to once a year, you may request that we disclose to you the categories and specific pieces of personal information we collected about you, the categories of sources from which your personal information was collected, and the business from which it was collected (if applicable). Deletion Request You have the right to request that we delete any personal information we collected from you and retained, unless an exception applies. Once we receive and confirm your verifiable consumer request, we will delete (and direct our service providers, subcontractors, and consultants to delete) your personal information, unless an exception applies.</p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">Exercise Your Rights</h2>\n" +
    " <p>You may exercise your rights (such as access and deletion) by submitting a verifiable consumer request to our email address: luna@302.AI. Only you may make a consumer request related to your personal information. Requests must:</p>\n" +
    " <ul>\n" +
    " <li>Provide sufficient information to allow us to reasonably verify you are the person making the request or an authorized representative.</li>\n" +
    " <li>Describe your request with sufficient detail for us to properly understand, evaluate, and respond to it.</li>\n" +
    " <li>We cannot respond to your request or provide you with personal information if we cannot verify your identity or authority to make the request and confirm that the personal information relates to you. Making a verifiable consumer request does not require you to create an account with us. We will only use personal information provided in a verifiable consumer request to verify the requester's identity or authority to make the request.</li>\n" +
    " </ul>\n" +
    "\n" +
    " <h2 style=\"text-align: Response Time and Format</h2>\n" +
    " <p>We aim to respond to a verifiable consumer request within 45 days of receiving it. If we require more time, we will notify you of the reason and the extension period in writing within the first 45 days. We will provide our written response by mail or electronically, at your option. Any disclosures we provide will only cover the 12-month period preceding the request. Whenever reasonably possible, we will provide your personal information in a readily usable format that allows you to transfer the information without hindrance. We will not charge a fee to process or respond to your verifiable consumer request unless it is excessive, repetitive, or manifestly unfounded. If we determine that the request warrants a fee, we will tell you why we made that decision and provide you with a cost estimate before completing your request. If denied, our response will explain why we cannot accommodate your request. Please note that these rights are not absolute and requests are subject to any applicable legal requirements, including legal and ethical reporting or document retention obligations.</p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">Non-Discrimination</h2>\n" +
    " <p>We will not discriminate against you if you, as a consumer, exercise any of your privacy rights. We will not:</p>\n" +
    " <ul>\n" +
    " <li>Deny you a good or service.</li>\n" +
    " <li>Charge you a different price or rate for goods or services, including by granting a discount or other benefit, or imposing a penalty.</li>\n" +
    " <li>Provide you with a different level or quality of goods or services.</li>\n" +
    " <li>Suggest that you may receive a different price or rate for goods or services or a different level or quality of goods or services.</li>\n" +
    " </ul>\n" +
    "</body>\n" +
    "\n" +
    "</html>\n".trimIndent()
    val privacyMsgJa = "<!DOCTYPE html>\n" +
            "<html lang=\"zh-CN\">\n" +
            "\n" +
            "<head>\n" +
            "  <meta charset=\"UTF-8\">\n" +
            "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "  <title>プライバシーポリシー</title>\n" +
            "  <style>\n" +
            "    body {\n" +
            "      font-family: \"Microsoft YaHei\", sans-serif;\n" +
            "      line-height: 1.6;\n" +
            "      margin: 0 auto;\n" +
            "      max-width: 800px;\n" +
            "      padding: 20px;\n" +
            "    }\n" +
            "\n" +
            "    h1,\n" +
            "    h2 {\n" +
            "      text-align: center;\n" +
            "    }\n" +
            "\n" +
            "    h3 {\n" +
            "      margin-top: 20px;\n" +
            "    }\n" +
            "\n" +
            "    p {\n" +
            "      margin: 10px 0;\n" +
            "    }\n" +
            "\n" +
            "    ul {\n" +
            "      margin: 10px 0 10px 20px;\n" +
            "    }\n" +
            "\n" +
            "    li {\n" +
            "      margin-bottom: 8px;\n" +
            "    }\n" +
            "  </style>\n" +
            "</head>\n" +
            "\n" +
            "<body>\n" +
            "  <h1>プライバシーポリシー</h1>\n" +
            " <p style=\"text-align: center;\">2022年9月22日</p>\n" +
    "\n" +
    " <p>302.AIはデータビジネスに携わっており、データプライバシーを非常に重視しています。最高水準のプライバシー基準を維持しながら、お客様や企業にクリーンでコンプライアンスに準拠した倫理的なデータを提供しています。当社の製品とサービスは世界中で展開されているため、変化し続ける世界のプライバシー環境を継続的に監視し、プライバシー法の完全な遵守を確保するために適切な調整を行っています。本プライバシーポリシーは、当社のサービスを積極的に利用することを選択したユーザー（下記参照）と、当社が事業の過程で個人データを処理する可能性のある個人の両方に適用されます。</p>\n" +
    "\n" +
    " <p>プライバシーポリシーの詳細な内訳はいつでもご覧いただけます。また、当社のポリシーやデータ規制の遵守についてご質問がある場合は、お問い合わせください。</p>\n" +
    "\n" +
    " <p>302.AI（以下「302.AI」、「当社」）は、ウェブサイト302.AI（以下「本サイト」といいます。）は、本サイトに関連するサービス、機能、コンテンツ、およびアプリケーション（以下総称して「本サービス」といいます。）を提供しています。当社はお客様のプライバシーを重視しており、本サービスをご利用いただくかどうかの判断材料として、また当社のプライバシー保護に関する必要な情報をすべて提供するために、本プライバシーポリシーを策定しました。本サービスにアクセスしてご利用いただくこと、またはお客様に該当するその他の形式で当社に情報を提供することにより、お客様は、本プライバシーポリシー（随時改訂されるものを含む）の条項に同意し、承諾するとともに、本プライバシーポリシーに定められた方法による情報の収集および使用に同意するものとします。本プライバシーポリシーをよくお読みいただき、定期的に参照して、その内容およびその後の変更点についてご理解いただくことをお勧めします。本プライバシーポリシーの条項に同意いただけない場合は、直ちに本サービスのご利用を中止し、関連情報を削除してください。</p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">どのような種類の情報を収集しますか？</h2>\n" +
    " <p>お客様が本サービスをご利用になる際、当社はお客様から以下の種類のデータを収集します。 （「ユーザーデータ」）：</p>\n" +
    " <ul>\n" +
    " <li>非個人情報。これは、ユーザーのアクティビティによって生成される、個人を特定できない非識別情報です。このような非個人情報には、ブラウザの種類、アクセスしたウェブページ、そのページでの滞在時間、アクセス日時などの情報が含まれる場合があります。</li>\n" +
    " <li>個人情報。個人情報とは、お客様を特定する、または特定する可能性のある情報です。当社が収集・保有する個人情報には、お客様のIPアドレス、氏名、メールアドレス、お支払い・請求情報、またはオンボーディングプロセスやサービス提供の一環として当社が随時要求するその他の情報が含まれます。本サービスでアカウントを作成する際、Gmail®などの指定されたサードパーティのウェブサイトまたはサービス（「サードパーティアカウント」）の認証情報を使用することがあります。これにより、お客様のアカウントをサードパーティアカウントにリンクできるようになります。このオプションを選択すると、サードパーティアカウントのポップアップボックスが表示されます。承認を求める画面が表示され、続行する前に承認が必要となり、当社が取得する情報の種類が説明されます。この情報には、ユーザー名、メールアドレス、プロフィール写真など、サードパーティアカウントに保存されている個人情報が含まれます。個人情報に明示的にリンクされている、または個人情報にリンクされている匿名情報は、そのような接続またはリンクが存在する限り、個人情報として扱います。</li>\n" +
    " <li>ソーシャルネットワークアカウントを介した登録：お客様がソーシャルネットワークアカウント（Facebook、Google+など）を介して本サービスに登録またはログインする場合、当社はお客様のソーシャルネットワークアカウントに関する基本情報（メールアドレスなど）およびお客様がそのアカウントで公開した、または当社と共有することに同意したその他の情報にアクセスできるようになります。当社は常に、ソーシャルネットワークプラットフォームの利用規約および制限事項を遵守します。</li>\n" +
    " </ul>\n" +
    " <p>当社が非個人情報と個人情報を組み合わせる場合、組み合わせた情報は個人情報として扱います。また、当社は様々なオンラインソースから公開されている個人データを収集する場合もあります。ほとんどの場合、場合によっては、この情報には氏名、メールアドレスなどの基本的な連絡先情報（「公開データ」）が含まれます。</p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">処理の法的根拠</h2>\n" +
    " <p>ユーザーデータの処理は、お客様に対する当社の契約上の義務の履行、サービスの提供、当社の正当な利益の保護、および当社の法的義務の遵守に必要です。</p>\n" +
    " <p>公開データの処理は、お客様の基本的な権利と自由を侵害しない限り、当社の正当な利益に基づいています。</p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">お客様の情報はどのように使用されますか？</h2>\n" +
    " <p>当社は、お客様にサービスを提供するため、および当社の法的要件と社内ガイドラインを遵守するために、ユーザーデータを使用します。つまり、当社は、お客様のアカウントの設定、当社は、サービスに関する最新情報、マーケティングオファー、またはお客様から寄せられた懸念事項についてお客様にご連絡し、サービスの向上を目的とした統計および分析調査を実施します。当社は、ユーザーにサービスを提供するために公開データを使用する場合があります。</p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">共有する情報</h2>\n" +
    " <p>当社は、ユーザーデータを貸与または販売することはありません。当社は、サービスを提供するために、ユーザーと公開データを共有する場合があります。当社は、お客様にサービス、保管、分析を提供するため、および当社の法的要件と社内ガイドラインを遵守するために、信頼できる他の第三者サービスプロバイダーまたはパートナーに個人情報を開示する場合があります。また、当社は、子会社および関連会社に個人情報を転送または開示する場合があります。</p>\n" +
    " <p>上記にかかわらず、当社はお客様のIPアドレスを他の顧客に開示することはありません。ただし、他の顧客（ネットワーク内の他のピアなど）がお客様のIPアドレスを使用してネットワークにアクセスした場合、例えば現在のIPアドレスを検索するなどして、お客様のIPアドレスを確認する場合があります。 </p>\n" +
    " <p>当社は、以下のいずれかの理由により必要であると誠実に判断した場合、特別な状況においてお客様の個人情報およびその他の情報を共有することがあります。(1) 法律、規制、召喚状、または裁判所命令を遵守するため。</p>\n" +
    " <p>詐欺、セキュリティ、当社のポリシー違反、または技術的な問題を検出、防止、またはその他の方法で対処するため。潜在的な違反の調査を含む、本プライバシーポリシーまたはお客様と当社との間のその他の契約の条項を執行するため。当社、当社のパートナー、当社の関連会社、当社のユーザー、または公衆の権利、財産、または安全に対する危害から保護するため。</p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">セキュリティと機密保持</h2>\n" +
    " <p>当社は、業界標準の情報、セキュリティツール、対策、および社内手順と厳格なガイドラインを用いて、情報の悪用とデータ漏洩を防止しています。当社の従業員は機密保持義務を負っています。当社が使用する対策と手順により、データの不正使用のリスクはありますが、システムの絶対的なセキュリティを保証することはできません。データ漏洩やセキュリティ上の脆弱性の可能性に気付いた場合は、直ちにご連絡ください。予防措置を含め、インシデントの調査に必要なすべての措置を講じます。</p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">お客様の選択と権利</h2>\n" +
    " <p>当社は、正当な事業目的または法的目的で情報を保持する必要がある場合を除き、お客様が情報を迅速に更新または削除できる方法を提供するよう努めています。お客様の個人情報を更新する際は、お客様のリクエストに対応する前に、ご本人確認をお願いする場合があります。</p>\n" +
    " <p>お客様の個人情報には、特に以下の権利が適用されますのでご注意ください。</p>\n" +
    " <ul>\n" +
    " <li>お客様の個人情報が処理されていることの確認を受け取り、保存されている個人情報にアクセスしてその情報を補足する権利。</li>\n" +

    " <li>お客様が直接、自発的に提供した個人情報のコピーを受け取る権利。構造化され、一般的に使用され、機械で読み取り可能な形式で当社に提供してください。</li>\n" +
    " <li>当社に個人情報の訂正を依頼する。</li>\n" +
    " <li>個人情報の消去を依頼する。</li>\n" +
    " <li>当社による個人情報の処理に異議を申し立てる。</li>\n" +
    " <li>当社による個人情報の処理の制限を依頼する。</li>\n" +
    " <li>監督機関に苦情を申し立てる。</li>\n" +
    " </ul>\n" +
    " <p>ただし、これらの権利は絶対的なものではなく、当社の正当な利益および規制要件の対象となる場合があることにご注意ください。この権利を行使するには、luna@302.AIまでご連絡ください。</p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">保持</h2>\n" +
    " <p>当社は、お客様の個人情報を当社は、本サービスを提供するために必要な期間、および当社の法的義務の遵守、紛争の解決、および当社のポリシーの施行に必要な期間、情報を保持します。保持期間は、収集された情報の種類と収集目的に基づいて決定され、状況に適用される要件、および古くなった未使用の情報を可能な限り速やかに破棄する必要性を考慮します。当社は、独自の裁量により、いつでも不完全または不正確な情報を修正、補足、または削除することができます。</p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">情報保証免責事項</h2>\n" +
    " <p>本ポリシーの他の規定にかかわらず、当社は、当社が収集、保管、およびお客様またはその他の者に開示する情報の正確性、正当性、またはセキュリティについて責任を負いません。当社のポリシーは、お客様から収集した情報の使用および開示のみを対象としています。お客様がインターネットを介して他の当事者またはウェブサイトに情報を開示する場合、お客様が開示した情報の使用または開示には異なる規則が適用される場合があります。したがって、利用規約をよくお読みになり、お客様が情報を開示することを選択した各第三者のプライバシーポリシーをご確認ください。</p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">情報保証免責事項</h2>\n" +
    " <p>本ポリシーの他の規定にかかわらず、当社は、当社が収集、保管、およびお客様またはその他の者に開示するいかなる情報の正確性、正しさ、またはセキュリティについても責任を負いません。当社のポリシーは、当社がお客様から収集する情報の使用および開示のみを対象としています。お客様がインターネットを介して他の当事者またはウェブサイトに情報を開示する場合、お客様が開示した情報の使用または開示には、異なる規則が適用される場合があります。したがって、お客様が情報を開示することを選択した各第三者の利用規約およびプライバシーポリシーをお読みいただくことをお勧めします。left;\">所有権の変更</h2>\n" +
    " <p>当社は、買収、合併、または売却などを含む、302.AIの全部または一部の所有権または管理権が変更された場合、当社のシステムに保存されているユーザーデータおよび公開データの全部または一部を譲渡する権利を留保します。破産、支払不能、または清算の場合、当社がお客様の個人情報の利用および移転を管理できなくなる可能性があることをお客様は承認するものとします。</p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">本プライバシーポリシーの変更</h2>\n" +
    " <p>当社は、独自の裁量により本ポリシーを随時改訂することがありますので、定期的にご確認ください。最新の改訂は「最終更新日」の見出しに反映されます。本ポリシーの変更は、サイトに掲載されます。変更後も引き続き本サービスをご利用いただく場合、改訂後のプライバシーポリシーに同意したものとみなされます。</p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">お問い合わせ</h2>\n" +
    " <p>お客様のプライバシーが当社のプライバシーポリシーに従って取り扱われていないと思われる場合、または本サービスのご利用中にお客様のプライバシーが第三者によって侵害されたと思われる場合は、302.AI（luna@302.AI）までご連絡ください。当社のプライバシー担当者が速やかに調査いたします。ユーザーの権利</p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">個人情報へのアクセス</h2>\n" +
    " <p>お客様は、年に1回まで、当社は、お客様について収集した個人情報の種類と具体的な内容、お客様の個人情報が収集された情報源の種類、および該当する場合は、その収集元となった事業体を開示します。削除要求 お客様は、例外が適用されない限り、当社がお客様から収集し、保有している個人情報の削除を要求する権利を有します。当社は、お客様からの検証可能な消費者要求を受領し、確認した後、例外が適用されない限り、お客様の個人情報を削除し（および当社のサービスプロバイダー、下請け業者、コンサルタントに削除を指示します）。</p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">権利の行使</h2>\n" +
    " <p>お客様は、当社のメールアドレス（luna@302.AI）に検証可能な消費者要求を送信することにより、お客様の権利（アクセスや削除など）を行使することができます。お客様の個人情報に関する消費者要求は、お客様ご自身のみが行うことができます。要求には、以下の条件を満たす必要があります。</p>\n" +
    " <ul>\n" +
    " <li>十分な情報を提供するお客様がリクエストを行った本人または権限のある代理人であることを合理的に確認できる情報。</li>\n" +
    " <li>リクエストを適切に理解、評価、および対応するために、リクエストの内容を十分に詳細に記述してください。</li>\n" +
    " <li>お客様の身元またはリクエストを行う権限を確認し、個人情報がお客様に関連していることを確認できない場合、リクエストに対応したり、個人情報を提供したりすることはできません。検証可能な消費者リクエストを行うために、お客様がアカウントを作成する必要はありません。検証可能な消費者リクエストで提供された個人情報は、リクエストを行った本人の身元またはリクエストを行う権限を確認するためにのみ使用します。検証可能な消費者リクエストで提供された個人情報は、リクエストを行った本人の身元またはリクエストを行う権限を確認するためにのみ使用します。</li>\n" +
    "</ul>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">応答時間と形式</h2>\n" +
    " <p>当社は、検証可能な消費者からのリクエストは、受領後45日以内にご返信ください。さらに時間を要する場合は、最初の45日以内に書面で理由と延長期間をお知らせいたします。書面による回答は、お客様の選択により、郵送または電子メールでお送りいたします。開示対象は、リクエスト発生前の12ヶ月間に限られます。合理的に可能な限り、お客様の個人情報は、お客様が支障なく情報を転送できる、使いやすい形式でご提供いたします。検証可能な消費者からのリクエストが過度、重複、または明らかに根拠がない限り、リクエストの処理または対応に料金を請求することはありません。リクエストに料金がかかると判断した場合は、その理由を説明し、リクエストを処理する前に費用の見積もりを提示いたします。リクエストが却下された場合は、リクエストに対応できない理由を回答でご説明いたします。これらの権利は絶対的なものではなく、リクエストは、法的および倫理的な報告義務や文書保管義務など、適用される法的要件の対象となることにご注意ください。</p>\n" +
    "\n" +
    " <h2 style=\"text-align: left;\">差別禁止</h2>\n" +
    " <p>お客様が消費者としてプライバシー権を行使された場合、当社はお客様に対して差別的な扱いをしません。当社は以下の行為を行いません。</p>\n" +
    " <ul>\n" +
    " <li>商品またはサービスの提供を拒否すること。</li>\n" +
    " <li>割引やその他の特典の付与、またはペナルティの適用などにより、商品またはサービスに対して異なる価格または料率を請求すること。</li>\n" +
    " <li>異なるレベルまたは品質の商品またはサービスを提供すること。</li>\n" +
    " <li>商品またはサービスに対して異なる価格または料率、あるいは異なるレベルまたは品質の商品またはサービスの提供を示唆すること。</li>\n" +
    " </ul>\n" +
    "</body>\n" +
    "\n" +
    "</html>\n".trimIndent()
    val useMsg = "<!DOCTYPE html>\n" +
            "<html lang=\"zh-CN\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>302.AI 使用条款</title>\n" +
            "  <style>\n" +
            "    body {\n" +
            "      font-family: \"Microsoft YaHei\", sans-serif;\n" +
            "      line-height: 1.6;\n" +
            "      margin: 0 auto;\n" +
            "      max-width: 800px;\n" +
            "      padding: 20px;\n" +
            "    }\n" +
            "\n" +
            "    h1,\n" +
            "    h2 {\n" +
            "      text-align: center;\n" +
            "    }\n" +
            "\n" +
            "    h3 {\n" +
            "      margin-top: 20px;\n" +
            "    }\n" +
            "\n" +
            "    p {\n" +
            "      margin: 10px 0;\n" +
            "    }\n" +
            "\n" +
            "    ul {\n" +
            "      margin: 10px 0 10px 20px;\n" +
            "    }\n" +
            "\n" +
            "    li {\n" +
            "      margin-bottom: 8px;\n" +
            "    }\n" +
            "  </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <h1>使用条款</h1>\n" +
            "    <div class=\"date\">2022年9月22日</div>\n" +
            "\n" +
            "    <div class=\"section\">\n" +
            "        <h2>用户需知</h2>\n" +
            "        <p>欢迎使用302.AI的使用条款。这是302.AI和302.AI网站的所有者和运营商以及所提供的任何相关可下载软件或服务（统称为\"平台\"）与您（\"您\"、\"您的\"），平台的用户。在本协议中，\"302.AI\"、\"我们\"、\"我们\"和\"我们的\"等词是指我们的公司302.AI。点击\"我同意\"、访问或使用平台，即表示您同意受本协议和隐私政策的约束。我们可能会修改我们的使用条款或隐私政策，并且在我们这样做时可能不会通知您。本协议的当前版本可在我们的网站上找到。您理解并同意，您有义务不时查看这些条款和条件，以便随时了解当前的规则和义务。您在对平台或本协议进行任何修订后继续使用平台，即表示您完全且不可撤销地接受任何及所有此类更改。请注意，仲裁和集体诉讼条款可能会影响您的权利。如果您不同意使用条款或隐私政策，请立即停止使用我们的平台。302.AI已开发、拥有并提供一项服务，该服务通过将用户的通信重定向到其他用户的设备（\"系统\"）来实现匿名浏览互联网。根据本协议，可用于商业用途。在本协议期限内，302.AI授权您仅出于客户内部业务运营的目的访问和使用系统。302.AI保留本协议未明确授予的任何和所有权利，包括但不限于对系统的任何和所有权利。</p>\n" +
            "    </div>\n" +
            "\n" +
            "    <div class=\"section\">\n" +
            "        <h2>用户信息和账户</h2>\n" +
            "        <p>在访问平台的某些部分之前，用户可能需要在平台上注册。我们将根据我们的隐私政策收集和披露您的信息。所有用户在注册我们的平台时都必须提供真实准确的信息，并且必须年满18岁。每个用户只能注册一个帐户。我们保留验证所有用户凭据并拒绝任何用户的权利。您对维护您的密码和帐户的机密性以及在您的帐户下发生的任何和所有活动负全部责任。您同意立即通知302.AI任何未经授权使用您的帐户或任何其他违反安全性的行为。302.AI不对因他人使用您的密码或帐户而造成的任何损失负责，无论您是否知情。如果您代表贵公司注册，您声明并保证您已获得贵公司授权代表贵公司创建账户，并且您声明并保证您已获得贵公司授权承担财务义务并具有法律约束力代表贵公司的协议。</p>\n" +
            "    </div>\n" +
            "\n" +
            "    <div class=\"section\">\n" +
            "        <h2>服务</h2>\n" +
            "        <p>通过平台，302.AI可以提供代理服务或其他服务。这些服务可以通过互联网或通过可下载的软件提供。如果您下载了我们的软件副本，即表示您同意遵守本协议。302.AI尽合理努力向您提供这些服务；但是，所提供的所有服务都取决于302.AI无法直接控制的无数因素和变量。出于这些原因，所提供的所有服务都不能得到保证，并且是\"按原样\"提供的。当您决定使用平台提供的任何服务时，您同意我们不做任何保证，包括但不限于访问或效率。您了解所提供的服务可能存在差异、不准确、造成负面影响或不正确。您同意免除我们因向您提供通过平台提供的任何服务而可能招致的任何责任。您同意在平台上发现的任何服务或任何其他信息可能不准确、未经证实甚至可能不正确。您同意免除我们因您使用我们的平台而可能承担的任何责任。</p>\n" +
            "    </div>\n" +
            "\n" +
            "    <div class=\"section\">\n" +
            "        <h2>用户内容</h2>\n" +
            "        <p>您通过平台提交或传输任何信息的能力，包括但不限于数据、信息、图像、参考资料或任何其他信息，在本协议中统称为\"用户内容\"。请注意，我们不需要托管、显示、迁移或分发您的任何用户内容，我们可能会拒绝接受或传输任何用户内容。您同意您对提交的任何用户内容承担全部责任，并且您免除我们与提交的任何用户内容相关的任何责任。我们为我们的平台提供行业标准的安全性，但我们不能保证任何此类用户内容的绝对安全性。任何被发现违反本协议或我们确定对平台有害的用户内容都可能被修改、编辑、在向我们的平台提交任何用户内容时，您声明并保证您拥有用户内容的所有权利，并且您已付费或有权使用提交的任何用户内容。此外，您声明并保证所有用户内容都是合法的，并且用户内容不会干扰任何第三方的权利或义务。当您向我们提交任何用户内容时，即表示您授予302.AI、其合作伙伴、关联公司、用户、代表和受让人非排他的、有限的、全额支付的、免税的、可撤销的、全球范围内的、通用的、可转让的、可转让的显示、分发、存储、广播、传输、复制、修改、准备衍生品或使用和重用您的全部或部分用户内容的许可，以便为您提供与平台相关的任何服务。此外，您授予302.AI全球性、永久、不可撤销、免税许可，以使用您提供的与我们平台运营相关的任何建议、增强请求、推荐、更正或其他反馈并将其纳入平台。</p>\n" +
            "    </div>\n" +
            "\n" +
            "    <div class=\"section\">\n" +
            "        <h2>隐私政策</h2>\n" +
            "        <p>我们重视您的隐私并了解您的隐私问题。我们的隐私政策已纳入本协议，它管理您对平台的访问和使用。请查看我们的隐私政策，以便您了解我们的隐私惯例。我们收集的所有信息均受我们的隐私政策约束，使用平台即表示您同意我们根据隐私政策对您的信息采取的所有行动。您进一步了解，302.AI收集的任何信息可能会转移到您的居住管辖区和/或其他国家/地区之外，以供302.AI及其关联公司存储、处理和使用。</p>\n" +
            "    </div>\n" +
            "\n" +
            "    <div class=\"section\">\n" +
            "        <h2>平台可用性</h2>\n" +
            "        <p>尽管我们尝试为您提供持续的可用性，但我们不保证平台在任何特定时间始终可用、工作或可访问。此外，302.AI没有义务向任何用户提供服务，并且可以随时自行决定暂停用户对平台的访问。只有有资格使用我们平台的用户才能这样做，并且我们可以随时拒绝服务或终止您的访问。我们不能保证在我们的平台上找到的任何东西都能满足您所需的功能或为您提供任何所需的结果。</p>\n" +
            "    </div>\n" +
            "\n" +
            "    <div class=\"section\">\n" +
            "        <h2>平台改造</h2>\n" +
            "        <p>我们保留随时更改、修改、更新或删除我们的平台的权利。我们可能出于安全原因、知识产权、法律原因或其他各种原因自行决定对我们的平台进行此类修改，我们无需解释此类修改。例如，我们可能会提供更新以修复安全漏洞或响应法律要求。请注意，这是我们如何行使本节规定的权利的非约束性说明，本节中的任何内容均不强制我们出于安全、法律或其他目的而采取措施更新平台。</p>\n" +
            "    </div>\n" +
            "\n" +
            "    <div class=\"section\">\n" +
            "        <h2>平台限制</h2>\n" +
            "        <p>您同意在使用和访问我们的平台时遵守所有限制。</p>\n" +
            "    </div>\n" +
            "\n" +
            "    <div class=\"section\">\n" +
            "        <h2>付款</h2>\n" +
            "        <p>平台的某些部分或提供的特定服务可能需要付款，您同意支付列出的所有成本、费用和税款。用户授权302.AI或其第三方支付处理商在购买时以他们的支付方式收费。请注意，购买是通过我们的第三方支付处理器完成的。在适用的情况下，您必须同意我们的第三方支付处理商处理付款的条款和条件。您提供的与购买或交易相关的所有信息必须准确、完整和最新。如果您未能付款或付款逾期，302.AI可以暂停或终止您对平台任何服务的访问，而不对我们承担任何责任。</p>\n" +
            "    </div>\n" +
            "\n" +
            "    <div class=\"section\">\n" +
            "        <h2>税收</h2>\n" +
            "        <p>如果302.AI不向您收取任何购买税费，则您同意为您使用和购买服务支付任何和所有适用的税费。此外，如果我们要求，您同意向我们提供税务文件以支持任何按时纳税的主张。</p>\n" +
            "    </div>\n" +
            "\n" +
            "    <div class=\"section\">\n" +
            "        <h2>定价和价格上涨</h2>\n" +
            "        <p>302.AI代理平台上列出了任何服务的定价。302.AI可以自行决定提高任何服务的价格，并且我们保留随时这样做的权利。如果价格上涨，302.AI将通知您，您将有机会接受或拒绝任何价格上涨。如果您打算拒绝涨价，请通知我们。如果您拒绝涨价，您可能无法访问平台的某些部分。您同意302.AI没有义务以最初在您注册时提供的价格提供任何服务。</p>\n" +
            "    </div>\n" +
            "\n" +
            "    <div class=\"section\">\n" +
            "        <h2>退款</h2>\n" +
            "        <p>由于我们提供在线服务，我们无法为任何付费服务提供退款。详情请查看退款协议。</p>\n" +
            "    </div>\n" +
            "\n" +
            "    <div class=\"section\">\n" +
            "        <h2>知识产权</h2>\n" +
            "        <p>\"302.AI\"名称、302.AI平台以及302.AI平台的设计以及其中包含的任何文本、文字、图像、模板、脚本、图形、交互功能和任何商标或徽标（\"标记\"）均归所有由302.AI授权或授权给302.AI，但须遵守国际法和国际公约规定的版权和其他知识产权。302.AI保留平台中未明确授予的所有权利。您同意不使用、复制或分发平台中包含的任何内容，除非我们已明确书面许可。</p>\n" +
            "    </div>\n" +
            "\n" +
            "    <div class=\"section\">\n" +
            "        <h2>免责声明</h2>\n" +
            "        <p>平台和所有服务均按\"原样\"、\"可用\"和\"存在所有故障\"的基础提供。在法律允许的最大范围内，302.AI代理人以及我们的任何员工、经理、管理人员、受让人或代理人均不对以下内容作出任何形式的陈述或保证或认可，无论明示或暗示：(1) 平台 (2) 通过平台提供的任何信息 (3) 服务 (4) 与向302.AI或通过平台传输信息相关的安全性</p>\n" +
            "        <p>此外，我们否认所有明示或暗示的保证，包括但不限于适销性、特定用途的适用性、利润损失、不侵权、所有权、定制、贸易、安静享受、系统集成和自由的保证从计算机病毒。</p>\n" +
            "    </div>\n" +
            "\n" +
            "    <div class=\"section\">\n" +
            "        <h2>责任限制</h2>\n" +
            "        <p>可能通过第三方传输到或通过我们的平台，任何有意或无意的服务失败或中断，或与任何第三方版权或其他知识产权所有者相关的任何行动。前述责任限制应在适用司法管辖区法律允许的最大范围内适用。某些司法管辖区不允许限制或排除附带间接损害的责任，因此上述限制或排除可能不适用于您。具体而言，在不允许的司法管辖区，我们不对以下情况承担责任：(1) 302.AI或其任何官员、员工或代理人的疏忽造成的死亡或人身伤害</p>\n" +
            "        <p>(1) 欺诈性虚假陈述 (2) 现在或将来排除的任何不合法的责任。如果不允许完全免责声明，您同意我们对您的总责任不得超过您在过去一个月内为使用我们的平台（包括我们的服务）所支付的金额。</p>\n" +
            "    </div>\n" +
            "\n" +
            "    <div class=\"section\">\n" +
            "        <h2>赔偿</h2>\n" +
            "        <p>您同意就任何和所有索赔、损害、义务、损失、责任、成本或债务和费用（包括但不限于律师的费用）产生于：·您使用和访问302.AI代理平台，包括任何服务。·您违反本协议的任何条款。·您与任何其他用途或第三方的互动。·您侵犯任何第三方权利，包括但不限于任何版权、财产或合同权利。此辩护和赔偿义务将在本协议和您对302.AI代理平台的使用中继续有效。您还同意，您有责任为我们辩护以应对此类索赔，并且在此类情况下，我们可能会要求您支付我们选择的律师费用。您同意此赔偿延伸至要求您支付我们合理的律师费、法庭费用和支出。如果出现本段所述的索赔，我们可以选择与提出索赔的一方或多方和解，您应对损失承担责任，就好像我们已经进行了审判一样。</p>\n" +
            "    </div>\n" +
            "\n" +
            "    <div class=\"section\">\n" +
            "        <h2>版权</h2>\n" +
            "        <p>我们非常重视侵犯版权。如果您认为您拥有的任何内容受到侵犯，请给我们发送一条消息，其中包含：·您的姓名。·版权受到侵犯的一方的名称（如果与您的姓名不同）。·被侵权作品的名称和描述。·侵权副本在我们平台上的位置。·声明您有充分的理由相信上述版权作品的使用未经版权所有者（或代表版权所有者的合法授权的第三方）授权，并且在其他方面不允许使用依法。·一份声明，您宣誓，本通知中包含的信息是准确的，并且您是版权所有者或在法律上拥有对其使用提起侵权诉讼的专有权利，如果做伪证，将受到处罚。您必须签署此通知并将其发送给我们的版权代理：302.AI的版权代理。反通知如果您收到来自302.AI的通知，说明您发布的内容已被删除，您可以通过提交反通知来回应。您的抗辩通知必须包含以下内容：·您的姓名、地址、电子邮件或电子签名。·通知参考编号（如果适用）。·识别材料及其在被移除之前的位置。·一份受伪证处罚的声明，说明材料因错误或错误识别而被删除。·您同意接受提交删除通知的一方的送达。请注意，除非您的通知严格符合上述要求，否则我们可能不会对您的反通知采取任何行动。请按照上述删除通知说明发送此抗辩通知。</p>\n" +
            "    </div>\n" +
            "\n" +
            "    <div class=\"section\">\n" +
            "        <h2>终止</h2>\n" +
            "        <p>您可以随时通过联系我们的客户支持取消您的付费订阅或您的帐户。任何退款均受本协议中包含的退款条款的约束。请注意，在您的帐户终止后，对我们平台部分的访问可能会立即被禁用。如果我们确定：</p>\n" +
            "        <ul>\n" +
            "            <li>您在使用我们的平台时违反了任何适用的法律，我们可以终止与您的本协议；</li>\n" +
            "            <li>如您违反本协议或在平台允许的情况下；或</li>\n" +
            "            <li>如果我们认为您的任何行为可能在法律上损害302.AI或我们的商业利益，由我们自行决定或酌情决定。如果终止，我们将努力为您提供及时的解释；但是，我们不需要这样做。</li>\n" +
            "        </ul>\n" +
            "    </div>\n" +
            "\n" +
            "    <div class=\"section\">\n" +
            "        <h2>完整协议</h2>\n" +
            "        <p>本协议连同隐私政策构成双方之间关于此处标的物的完整且排他性的理解和协议，并取代所有先前或同时期的与其标的物相关的书面或口头协议或理解。对本协议任何条款的任何弃权、修改或修正只有在以书面形式并由各方正式授权的代表签署后才有效。</p>\n" +
            "    </div>\n" +
            "\n" +
            "    <div class=\"section\">\n" +
            "        <h2>修订协议</h2>\n" +
            "        <p>我们可能会不时修改本协议。当我们修改本协议时，我们将更新此页面并指明上次修改的日期，或者我们可能会向您发送电子邮件。您可以拒绝同意修改，但如果您同意，您必须立即停止使用我们的平台和我们的平台。</p>\n" +
            "    </div>\n" +
            "\n" +
            "    <div class=\"section\">\n" +
            "        <h2>当事人关系</h2>\n" +
            "        <p>各方为独立承包商。本协议不会在您与302.AI之间建立合伙、特许经营、合资、代理、信托或雇佣关系。</p>\n" +
            "    </div>\n" +
            "\n" +
            "    <div class=\"section\">\n" +
            "        <h2>反滥用</h2>\n" +
            "        <p>您同意遵守我们的反滥用政策。如果您发现任何用户滥用我们的服务，请联系我们的客户支持：luna@302.AI**。</p>\n" +
            "    </div>\n" +
            "\n" +
            "    <div class=\"section\">\n" +
            "        <h2>第三方内容</h2>\n" +
            "        <p>我们不控制您在使用平台时访问、下载、接收或购买的任何数据、内容、服务或产品（包括软件），也不对其负责。我们可以但没有任何义务阻止信息、传输或访问某些信息、服务、产品或域，以保护平台、我们的网络、公众或我们的用户。我们不是通过平台访问的第三方内容的发布者，并且不对提供给或由其提供的任何意见、建议、声明、消息、服务、图形、数据或任何其他信息的内容、准确性、及时性或交付负责可通过平台访问的第三方。</p>\n" +
            "    </div>\n" +
            "    \n" +
            "    <div class=\"section\">\n" +
            "        <h2>地理访问</h2>\n" +
            "        <p>平台不受地域限制；但是，我们不对平台适合在您所在的位置和管辖范围内使用或访问做出任何陈述或保证。您主动访问和使用您所在国家/地区的平台，并且您全权负责遵守您当地的法律法规（如果此类法律适用）。我们保留随时自行决定将平台或其任何部分的可用性限制为任何人、实体、地理区域或司法管辖区的权利。</p>\n" +
            "    </div>\n" +
            "    \n" +
            "    <div class=\"section\">\n" +
            "        <h2>平台问题和支持</h2>\n" +
            "        <p>如果您在访问或使用平台时遇到问题，请联系我们的客户支持：luna@302.AI。</p>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>\n".trimIndent()
    val useMsgEn = "<!DOCTYPE html>\n" +
            "<html lang=\"zh-CN\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>302.AI Terms of use</title>\n" +
            "  <style>\n" +
            "    body {\n" +
            "      font-family: \"Microsoft YaHei\", sans-serif;\n" +
            "      line-height: 1.6;\n" +
            "      margin: 0 auto;\n" +
            "      max-width: 800px;\n" +
            "      padding: 20px;\n" +
            "    }\n" +
            "\n" +
            "    h1,\n" +
            "    h2 {\n" +
            "      text-align: center;\n" +
            "    }\n" +
            "\n" +
            "    h3 {\n" +
            "      margin-top: 20px;\n" +
            "    }\n" +
            "\n" +
            "    p {\n" +
            "      margin: 10px 0;\n" +
            "    }\n" +
            "\n" +
            "    ul {\n" +
            "      margin: 10px 0 10px 20px;\n" +
            "    }\n" +
            "\n" +
            "    li {\n" +
            "      margin-bottom: 8px;\n" +
            "    }\n" +
            "  </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <h1>302.AI Terms of use</h1>\n" +
            " <div class=\"date\">September 22, 2022</div>\n" +
            "\n" +
            " <div class=\"section\">\n" +
            " <h2>Notice to Users</h2>\n" +
            " <p>Welcome to the Terms of Use of 302.AI. This is a contract between the owner and operator of 302.AI and the 302.AI website and any related downloadable software or services provided thereon (collectively, the 'Platform') and you ('you', 'your'), the user of the Platform. In this Agreement, the terms '302.AI', 'we', 'us', and 'our' refer to our company, 302.AI. By clicking 'I Agree', accessing or using the Platform, you agree to be bound by this Agreement and the Privacy Policy. We may modify our Terms of Use or Privacy Policy and may not notify you when we do so. The current version of this Agreement is available on our website. You understand and agree that it is your responsibility to review these terms and conditions from time to time to stay informed. Current rules and obligations. Your continued use of the Platform after any revisions to the Platform or this Agreement constitutes your full and irrevocable acceptance of any and all such changes. Please note that the arbitration and class action clauses may affect your rights. If you do not agree to the Terms of Use or Privacy Policy, please stop using our Platform immediately. 302.AI has developed, owns, and provides a service that enables anonymous browsing of the Internet by redirecting users' communications to other users' devices (the 'System'). It may be used for commercial purposes under this Agreement. During the term of this Agreement, 302.AI authorizes you to access and use the System solely for the purpose of Customer's internal business operations. 302.AI reserves any and all rights not expressly granted in this Agreement, including, but not limited to, any and all rights in and to the System. </p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>User Information and Accounts</h2>\n" +
    " <p>Before accessing certain parts of the Platform, users may be required to register on the Platform. We will collect and disclose your information in accordance with our Privacy Policy. All users must provide true and accurate information when registering on our Platform and must be at least 18 years old. Each user may only register one account. We reserve the right to verify all user credentials and reject any user. You are entirely responsible for maintaining the confidentiality of your password and account and for any and all activities that occur under your account. You agree to immediately notify 302.AI of any unauthorized use of your account or any other breach of security. 302.AI is not responsible for any losses that you incur as a result of someone else using your password or account, either with or without your knowledge. If you are registering on behalf of your company, you represent and warrant that you are authorized by your company to create an account on your company's behalf, and you represent and warrant that you are authorized by your company to incur financial obligations and enter into legally binding agreements on your company's behalf. </p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>Services</h2>\n" +
    " <p>Through the Platform, 302.AI may provide proxy services or other services. These services may be provided over the internet or through downloadable software. If you download a copy of our software, you agree to be bound by this Agreement. 302.AI uses reasonable efforts to provide these services to you; however, all services provided are subject to numerous factors and variables beyond 302.AI's direct control. For these reasons, all services provided are not guaranteed and are provided 'as is.' When you decide to use any service provided by the Platform, you agree that we make no guarantees, including, but not limited to, accessibility or efficiency. You understand that the services provided may contain discrepancies, inaccuracies, negative impacts, or be incorrect. You agree to release us from any liability you may incur as a result of providing you with any services offered through the Platform. You agree that any services or any other information found on the Platform may be inaccurate, unverified, or even incorrect. You agree to release us from any liability you may incur as a result of your use of our Platform. </p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>User Content</h2>\n" +
    " <p>Your ability to submit or transmit any information through the Platform, including but not limited to data, information, images, references or any other information, is collectively referred to in this Agreement as \"User Content\". Please note that we are not required to host, display, migrate or distribute any of your User Content, and we may refuse to accept or transmit any User Content. You agree that you are solely responsible for any User Content you submit, and you release us from any liability related to any User Content you submit. We provide industry-standard security for our Platform, but we cannot guarantee the absolute security of any such User Content. Any User Content found to violate this Agreement or that we determine to be harmful to the Platform may be modified, edited, or otherwise violated. In submitting any User Content to our Platform, you represent and warrant that you own all rights to the User Content and that you have paid for or have the right to use any User Content you submit. Any User Content. In addition, you represent and warrant that all User Content is legal and that the User Content does not interfere with the rights or obligations of any third party. When you submit any User Content to us, you grant 302.AI, its partners, affiliates, users, representatives and assigns a non-exclusive, limited, fully paid, royalty-free, revocable, worldwide, universal, transferable, sublicensable license to display, distribute, store, broadcast, transmit, copy, modify, prepare derivative works of, or use and reuse your User Content, in whole or in part, to provide you any services related to the Platform. In addition, you grant 302.AI a worldwide, perpetual, irrevocable, royalty-free license to use and incorporate into the Platform any suggestions, enhancement requests, recommendations, corrections or other feedback you provide in connection with the operation of our Platform. </p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>Privacy Policy</h2>\n" +
    " <p>We value your privacy and understand your privacy concerns. Our Privacy Policy is incorporated into this Agreement and governs your access to and use of the Platform. Please review our Privacy Policy so that you understand our privacy practices. All information we collect is subject to our Privacy Policy, and by using the Platform, you consent to all actions we take with respect to your information consistent with our Privacy Policy. You further understand that any information collected by 302.AI may be transferred outside of your jurisdiction of residence and/or other countries for storage, processing, and use by 302.AI and its affiliates. </p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>Platform Availability</h2>\n" +
    " <p>While we attempt to provide you with continuous availability, we do not guarantee that the Platform will always be available, functional, or accessible at any given time. Furthermore, 302.AI has no obligation to provide services to any user and may suspend a user's access to the Platform at any time in its sole discretion. Only users eligible to use our Platform may do so, and we may deny service or terminate your access at any time. We cannot guarantee that anything found on our Platform will function as you desire or provide you with any desired results. </p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>Platform Modifications</h2>\n" +
    " <p>We reserve the right to change, modify, update, or remove our Platform at any time. We may make such modifications to our Platform for security, intellectual property, legal, or other reasons at our sole discretion, and we are not required to explain such modifications. For example, we may provide updates to fix security vulnerabilities or respond to legal requests. Please note that this is a non-binding description of how we may exercise our rights under this section, and nothing in this section obligates us to take steps to update the Platform for security, legal, or other purposes. </p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>Platform Limitations</h2>\n" +
    " <p>You agree to abide by all limitations when using and accessing our Platform.</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>Payment</h2>\n" +
    " <p>Certain portions of the Platform or specific services offered may require payment, and you agree to pay all listed costs, fees, and taxes. Users authorize 302.AI or its third-party payment processor to charge their payment method at the time of purchase. Please note that purchases are made through our third-party payment processor. Where applicable, you must agree to the terms and conditions of our third-party payment processor for processing payments. All information you provide in connection with a purchase or transaction must be accurate, complete, and current. If you fail to make a payment or if a payment is overdue, 302.AI may suspend or terminate your access to any service on the Platform without any liability to us.</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>Taxes</h2>\n" +
    " <p>If 302.AI does not charge you any taxes on your purchases, you agree to pay any and all applicable taxes on your use and purchase of the Services. Furthermore, if requested by us, you agree to provide us with tax documentation to support any claim of timely tax payment.</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>Pricing and Price Increases</h2>\n" +
    " <p>The pricing for any Service is listed on the 302.AI Agent Platform. 302.AI may, at its sole discretion, increase the price of any Service, and we reserve the right to do so at any time. If a price increase occurs, 302.AI will notify you, and you will have the opportunity to accept or decline any price increase. If you intend to decline a price increase, please notify us. If you decline a price increase, you may be unable to access certain portions of the Platform. You agree that 302.AI has no obligation to provide any Service at the price initially offered when you register. </p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>Refunds</h2>\n" +
    " <p>Because we provide online services, we cannot provide refunds for any paid services. Please see the Refund Agreement for details.</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>Intellectual Property</h2>\n" +
    " <p>The 302.AI name, the 302.AI Platform, and the design of the 302.AI Platform, as well as any text, words, images, templates, scripts, graphics, interactive features, and any trademarks or logos contained therein (\"Marks\") are owned by or licensed to 302.AI, subject to copyright and other intellectual property rights under international law and international conventions. 302.AI reserves all rights in the Platform not expressly granted. You agree not to use, copy, or distribute any content contained in the Platform unless we have express written permission.</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>Disclaimer</h2>\n" +
    " <p>The Platform and all Services are provided on an 'as is,' 'as available,' and 'with all faults' basis. To the maximum extent permitted by law, neither 302.AI Agents nor any of our employees, managers, officers, assigns, or agents make any representations or warranties or endorsements of any kind, whether express or implied, regarding: (1) the Platform (2) any information provided through the Platform (3) the Services (4) the security associated with the transmission of information to or through 302.AI</p>\n" +
    " <p>Furthermore, we disclaim all warranties, express or implied, including, but not limited to, warranties of merchantability, fitness for a particular purpose, loss of profits, non-infringement, title, customization, trade, quiet enjoyment, system integration, and freedom from computer virus.</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>Limitation of Liability</h2>\n" +
    " <p>may be transmitted to or through our Platform by a third party, any intentional or unintentional failure or interruption of service, or any action related to any third party copyright or other intellectual property rights owner. The foregoing limitation of liability shall apply to the maximum extent permitted by law in the applicable jurisdiction. Some jurisdictions do not allow the limitation or exclusion of liability for incidental or consequential damages, so the above limitation or exclusion may not apply to you. Specifically, in jurisdictions that do not allow it, we are not liable for: (1) death or personal injury caused by the negligence of 302.AI or any of its officers, employees or agents.</p>\n" +
    " <p>(1) fraudulent misrepresentation (2) any liability that is unlawful to exclude now or in the future. If the complete disclaimer of liability is not permitted, you agree that our total liability to you shall not exceed the amount you paid for use of our Platform (including our services) in the past month.</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>Indemnity</h2>\n" +
    " <p>You agree to indemnify and hold us harmless from and against any and all claims, damages, obligations, losses, liabilities, costs or debt, and expenses (including, but not limited to, attorneys' fees) arising from: · Your use and access of the 302.AI Proxy Platform, including any Services. · Your violation of any term of this Agreement. · Your interactions with any other user or third party. · Your violation of any third-party right, including, but not limited to, any copyright, property, or contract right. This defense and indemnification obligation will survive this Agreement and your use of the 302.AI Proxy Platform. You also agree that you are responsible for defending us against such claims, and in such event, we may require you to pay our attorneys' fees of our choosing. You agree that this indemnification extends to requiring you to pay our reasonable attorneys' fees, court costs, and expenses. In the event of a claim described in this paragraph, we may choose to settle with the party or parties asserting the claim, and you will be liable for the damages as if we had gone to trial. </p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>Copyright</h2>\n" +
    " <p>We take copyright infringement very seriously. If you believe that any content you own has been infringed, please send us a message that includes: · Your name. · The name of the party whose copyright has been infringed (if different from your name). · The name and description of the work that has been infringed. · The location of the infringing copy on our Platform. · A statement that you have a good faith belief that use of the copyrighted work described above is not authorized by the copyright owner (or a third party legally authorized to act on the copyright owner's behalf) and that use is not otherwise permitted under the law. · A statement that you swear, under penalty of perjury, that the information contained in this notification is accurate and that you are the copyright owner or have the exclusive right under the law to bring an action for infringement regarding its use. You must sign this notification and include it in your email address. It should be sent to our Copyright Agent: 302.AI's Copyright Agent. Counter-Notification If you receive a notification from 302.AI stating that content you posted has been removed, you may respond by submitting a counter-notification. Your counter-notification must include the following: · Your name, address, email, or electronic signature. · Notification reference number (if applicable). · Identification of the material and its location before it was removed. · A statement under penalty of perjury that the material was removed by mistake or misidentification. · Your consent to accept service of process from the party that submitted the takedown notice. Please note that we may not take any action on your counter-notification unless your notice strictly complies with the requirements set forth above. Please follow the instructions for takedown notices above to send this counter-notification. </p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>Termination</h2>\n" +
    " <p>You may cancel your paid subscription or your account at any time by contacting our customer support. Any refund is subject to the refund terms contained in this Agreement. Please note that upon termination of your account, access to portions of our Platform may be immediately disabled. We may terminate this Agreement with you if we determine that:</p>\n" +
    " <ul>\n" +
    " <li>You have violated any applicable law in your use of our Platform;</li>\n" +
    " <li>If you breach this Agreement or as permitted by the Platform; or</li>\n" +
    " <li>If we believe that any of your actions may legally harm 302.AI or our business interests, as determined by us in our sole and absolute discretion. In the event of termination, we will endeavor to provide you with a prompt explanation; however, we are not required to do so.</li>\n" +
    " </ul>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>Entire Agreement</h2>\n" +
    " <p>This Agreement, together with the Privacy Policy, constitutes the complete and exclusive understanding and agreement between the parties with respect to the subject matter hereof and supersedes all prior or contemporaneous agreements or understandings, whether written or oral, relating to its subject matter. Any waiver, modification, or amendment of any provision of this Agreement will be effective only if in writing and signed by a duly authorized representative of each party.</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>Amended Agreement</h2>\n" +
    " <p>We may modify this Agreement from time to time. When we modify this Agreement, we will update this page and indicate the date it was last modified, or we may send you an email. You may refuse to agree to the modifications, but if you do, you must immediately cease using our Platform and our Services.</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>Relationship of the Parties</h2>\n" +
    " <p>The parties are independent contractors. This Agreement does not create a partnership, franchise, joint venture, agency, fiduciary, or employment relationship between you and 302.AI.</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>Anti-Abuse</h2>\n" +
    " <p>You agree to abide by our Anti-Abuse Policy. If you discover any user abusing our services, please contact our customer support at luna@302.AI**.</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>Third Party Content</h2>\n" +
    " <p>We do not control and are not responsible for any data, content, services, or products (including software) that you access, download, receive, or purchase while using the Platform. We may, but have no obligation to, block information, transmissions, or access to certain information, services, products, or domains to protect the Platform, our network, the public, or our users. We are not the publisher of third-party content accessed through the Platform and are not responsible for the content, accuracy, timeliness, or delivery of any opinions, advice, statements, messages, services, graphics, data, or any other information provided to or by third parties that may be accessed through the Platform. </p>\n" +
    " </div>\n" +
    " \n" +
    " <div class=\"section\">\n" +
    " <h2>Geographic Access</h2>\n" +
    " <p>The Platform is not geographically restricted; however, we make no representation or warranty that the Platform is appropriate for use or access in your location and jurisdiction. You access and use the Platform on your own initiative and are solely responsible for compliance with your local laws and regulations, if and to the extent such laws are applicable. We reserve the right to limit the availability of the Platform or any portion thereof to any person, entity, geographic area, or jurisdiction at any time and in our sole discretion. </p>\n" +
    " </div>\n" +
    " \n" +
    " <div class=\"section\">\n" +
    " <h2>Platform Issues and Support</h2>\n" +
    " <p>If you experience problems accessing or using the Platform, please contact our customer support at luna@302.AI. </p>\n" +
    " </div>\n" +
    "</body>\n" +
    "</html>\n".trimIndent()
    val useMsgJa = "<!DOCTYPE html>\n" +
            "<html lang=\"zh-CN\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>302.AI 利用規約</title>\n" +
            "  <style>\n" +
            "    body {\n" +
            "      font-family: \"Microsoft YaHei\", sans-serif;\n" +
            "      line-height: 1.6;\n" +
            "      margin: 0 auto;\n" +
            "      max-width: 800px;\n" +
            "      padding: 20px;\n" +
            "    }\n" +
            "\n" +
            "    h1,\n" +
            "    h2 {\n" +
            "      text-align: center;\n" +
            "    }\n" +
            "\n" +
            "    h3 {\n" +
            "      margin-top: 20px;\n" +
            "    }\n" +
            "\n" +
            "    p {\n" +
            "      margin: 10px 0;\n" +
            "    }\n" +
            "\n" +
            "    ul {\n" +
            "      margin: 10px 0 10px 20px;\n" +
            "    }\n" +
            "\n" +
            "    li {\n" +
            "      margin-bottom: 8px;\n" +
            "    }\n" +
            "  </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <h1>利用規約</h1>\n" +
    "<div class=\"date\">2022年9月22日</div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>ユーザーへのお知らせ</h2>\n" +
    " <p>302.AIの利用規約へようこそ。本規約は、302.AIおよび302.AIウェブサイト、ならびにそこで提供される関連ダウンロードソフトウェアまたはサービス（以下、総称して「プラットフォーム」といいます）の所有者および運営者と、プラットフォームのユーザーであるお客様（以下「お客様」といいます）との間の契約です。本契約において、「302.AI」、「当社」、「私たちの」および「当社の」という用語は、当社である302.AIを指します。「同意する」をクリックするか、プラットフォームにアクセスまたは使用することにより、お客様は本契約およびプライバシーポリシーに拘束されることに同意するものとします。当社は、利用規約またはプライバシーポリシーを変更する場合がありますが、その際にお客様に通知しない場合があります。本契約の最新版は、当社のウェブサイト。お客様は、最新情報を入手するために、本利用規約を随時確認する責任があることを理解し、同意するものとします。最新の規則および義務。プラットフォームまたは本契約の改訂後もプラットフォームを引き続きご利用いただくことは、かかる変更のすべてを完全にかつ取消不能に承諾したものとみなされます。仲裁および集団訴訟条項はお客様の権利に影響を与える可能性があることにご注意ください。利用規約またはプライバシーポリシーに同意しない場合は、直ちにプラットフォームのご利用を中止してください。302.AIは、ユーザーの通信を他のユーザーのデバイスにリダイレクトすることにより、匿名でのインターネット閲覧を可能にするサービス（以下「システム」）を開発、所有、提供しています。本システムは、本契約に基づき商業目的で使用することができます。本契約の期間中、302.AIは、お客様がお客様の社内業務運営のみを目的としてシステムにアクセスし、使用することを許可します。302.AIは、本契約で明示的に付与されていないすべての権利（システムに関するすべての権利を含みますが、これに限定されません）を留保します。</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>ユーザー情報とアカウント</h2>\n" +
    " <p>プラットフォームの特定の部分にアクセスする前に、ユーザーはプラットフォームへの登録を求められる場合があります。当社は、プライバシーポリシーに従ってお客様の情報を収集および開示します。すべてのユーザーは、当社のプラットフォームに登録する際に真実かつ正確な情報を提供する必要があり、18歳以上である必要があります。各ユーザーは1つのアカウントのみを登録できます。当社は、すべてのユーザー資格情報を検証し、ユーザーを拒否する権利を留保します。お客様は、パスワードとアカウントの機密性を維持し、お客様のアカウントで行われるすべての活動について全責任を負います。お客様は、アカウントの不正使用またはその他のセキュリティ侵害について、302.AIに直ちに通知することに同意します。302.AIは、お客様のパスワードまたはアカウントが、お客様の認識の有無にかかわらず、第三者によって使用された結果、お客様が被るいかなる損失についても責任を負いません。お客様が会社の代理で登録する場合、お客様は、会社のアカウントにアカウントを作成することを会社から承認されていることを表明し、保証するものとします。 302.AIは、本プラットフォームを通じて、代理サービスまたはその他のサービスを提供する場合があります。これらのサービスは、インターネット経由またはダウンロード可能なソフトウェアを通じて提供される場合があります。お客様が当社のソフトウェアをダウンロードした場合、お客様は本契約に拘束されることに同意するものとします。302.AIは、お客様にこれらのサービスを提供するために合理的な努力を払いますが、提供されるすべてのサービスは、302.AIの直接的な管理が及ばない多数の要因および変数の影響を受けます。これらの理由により、提供されるすべてのサービスは保証されておらず、「現状有姿」で提供されます。お客様が本プラットフォームが提供するサービスをご利用になる場合、お客様は、当社がアクセシビリティや効率性などを含むがこれらに限定されない一切の保証を行わないことに同意するものとします。お客様は、提供されるサービスに矛盾、不正確さ、悪影響、または誤りが生じる可能性があります。お客様は、プラットフォームを通じて提供されるサービスをお客様に提供した結果としてお客様が被る可能性のあるいかなる責任からも当社を免責することに同意するものとします。お客様は、プラットフォーム上のサービスまたはその他の情報が不正確、未検証、または不正確である可能性があることに同意するものとします。お客様は、当社のプラットフォームの利用の結果としてお客様が被る可能性のあるいかなる責任からも当社を免責することに同意するものとします。</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>ユーザーコンテンツ</h2>\n" +
    " <p>データ、情報、画像、参照、その他の情報を含むがこれらに限定されない、プラットフォームを通じてお客様が送信または送信する情報を、本契約において総称して「ユーザーコンテンツ」といいます。当社は、お客様のユーザーコンテンツをホスティング、表示、移行、または配布する義務を負わず、また、いかなるユーザーコンテンツの受領または送信も拒否する場合がありますのでご了承ください。お客様は、ユーザーコンテンツについて単独で責任を負うことに同意するものとします。お客様が提出するユーザーコンテンツに関する一切の責任は、お客様が当社に帰属するものとします。当社は、当社のプラットフォームに業界標準のセキュリティを提供しますが、ユーザーコンテンツの絶対的なセキュリティを保証することはできません。本契約に違反する、または当社がプラットフォームに有害であると判断するユーザーコンテンツは、変更、編集、またはその他の方法で侵害される可能性があります。お客様は、当社のプラットフォームにユーザーコンテンツを提出する際に、当該ユーザーコンテンツに関するすべての権利を所有し、提出するユーザーコンテンツの使用料を支払済みであるか、または使用する権利を有していることを表明し、保証するものとします。また、お客様は、すべてのユーザーコンテンツが合法であり、第三者の権利または義務を侵害しないことを表明し、保証するものとします。お客様が当社にユーザーコンテンツを提出する際には、302.AI、そのパートナー、関連会社、ユーザー、代理人、および譲受人に対し、お客様のユーザーコンテンツの全部または一部を表示、配布、保存、放送、送信、コピー、変更、二次的著作物の作成、または使用および再利用するための、非独占的、限定的、全額支払済み、ロイヤリティフリー、取消可能、全世界的、普遍的、譲渡可能、サブライセンス可能なライセンスを付与するものとします。プラットフォームに関連するあらゆるサービス。さらに、お客様は、プラットフォームの運営に関連してお客様が提供するあらゆる提案、機能強化の要望、推奨事項、修正、その他のフィードバックを、プラットフォームに使用および組み込むための、世界的、永続的、取消不能、ロイヤリティフリーのライセンスを302.AIに付与するものとします。</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>プライバシーポリシー</h2>\n" +
    " <p>当社はお客様のプライバシーを重視し、プライバシーに関する懸念を理解しています。当社のプライバシーポリシーは本契約に組み込まれており、お客様によるプラットフォームへのアクセスおよび利用に適用されます。当社のプライバシー慣行を理解するために、当社のプライバシーポリシーをご確認ください。当社が収集するすべての情報は当社のプライバシーポリシーの対象となり、お客様は、プラットフォームを使用することにより、当社のプライバシーポリシーに従ってお客様の情報に関して当社が行うすべての措置に同意するものとします。また、お客様は、302.AIが収集した情報が、302.AIによる保管、処理、および使用のために、お客様の居住地の管轄区域外および／またはその他の国に転送される可能性があることを了承するものとします。およびその関連会社。</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>プラットフォームの可用性</h2>\n" +
    " <p>当社は、お客様に継続的な可用性を提供するよう努めますが、プラットフォームが常に利用可能、機能的、またはアクセス可能であることを保証するものではありません。さらに、302.AIは、いかなるユーザーに対してもサービスを提供する義務を負わず、独自の裁量により、いつでもユーザーのプラットフォームへのアクセスを停止することができます。当社のプラットフォームを利用できるのは、当社のプラットフォームの利用資格を有するユーザーのみであり、当社はいつでもサービスを拒否したり、お客様のアクセスを停止したりすることができます。当社は、当社のプラットフォーム上のあらゆるものがお客様の期待どおりに機能し、またはお客様が望む結果をもたらすことを保証することはできません。</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>プラットフォームの変更</h2>\n" +
    " <p>当社は、プラットフォームをいつでも変更、修正、更新、または削除する権利を留保します。当社は、セキュリティ、知的財産、法的理由、その他の理由により、独自の裁量によりプラットフォームに変更を加えることができますが、かかる変更について説明義務を負いません。例えば、セキュリティ上の脆弱性を修正したり、法的要請に対応したりするためにアップデートを提供する場合があります。これは、本条項に基づく当社の権利の行使方法についての拘束力のない説明であり、本条項のいかなる内容も、当社がセキュリティ、法的理由、その他の目的でプラットフォームを更新する措置を講じる義務を負うものではないことにご注意ください。</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>プラットフォームの制限事項</h2>\n" +
    " <p>お客様は、当社のプラットフォームの利用およびアクセスに際して、すべての制限事項を遵守することに同意するものとします。</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>お支払い</h2>\n" +
    " <p>プラットフォームの一部または提供される特定のサービスには料金が発生する場合があり、お客様は記載されているすべての費用、手数料、および税金を支払うことに同意するものとします。ユーザーは、購入時に302.AIまたはそのサードパーティ決済処理業者がお客様の支払い方法に請求することを承認するものとします。購入はサードパーティ決済処理業者を通じて行われることにご注意ください。該当する場合、お客様は支払い処理に関するサードパーティ決済処理業者の利用規約に同意する必要があります。購入または取引に関連してお客様が提供するすべての情報は、正確、完全、かつ最新のものでなければなりません。お客様が支払いを行わない場合、または支払いが遅延した場合、302.AIは、当社に対する一切の責任を負うことなく、お客様のプラットフォーム上のサービスへのアクセスを停止または終了する場合があります。</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>税金</h2>\n" +
    " <p>302.AIがお客様に税金を請求しない場合、お客様は、本サービスの利用および購入に適用されるすべての税金を支払うことに同意するものとします。さらに、当社から要求された場合、お客様は、期限内の納税を裏付ける税務書類を当社に提供することに同意するものとします。</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>価格設定および値上げ</h2>\n" +
    " <p>サービスの価格は、302.AIエージェントプラットフォームに掲載されています。302.AIは、独自の裁量により、サービスの価格をいつでも値上げすることができ、当社はいつでも値上げを行う権利を留保します。値上げが行われた場合、302.AIはお客様に通知し、お客様は値上げを承認または拒否する機会が与えられます。値上げを拒否する場合は、当社までご連絡ください。値上げを拒否した場合、プラットフォームの一部にアクセスできなくなる可能性があります。お客様は、302.AIが当初提示した価格でサービスを提供する義務を負わないことに同意するものとします。登録時に。</p>\n" +

    " </div>\n" +

    "\n" +

    " <div class=\"section\">\n" +

    " <h2>払い戻し</h2>\n" +

    " <p>当社はオンラインサービスを提供しているため、有料サービスの払い戻しはできません。詳細は払い戻し契約をご覧ください。</p>\n" +

    " </div>\n" +

    "\n" +

    " <div class=\"section\">\n" +

    " <h2>知的財産権</h2>\n" +
    "<p>「302.AI」の名称、302.AIプラットフォーム、および302.AIプラットフォームのデザイン、ならびにそこに含まれるあらゆるテキスト、単語、画像、テンプレート、スクリプト、グラフィック、インタラクティブ機能、およびあらゆる商標またはロゴ（以下「マーク」）は、国際法および国際条約に基づく著作権およびその他の知的財産権の対象となり、302.AIが所有またはライセンス供与しています。302.AIは、プラットフォームにおいて明示的に付与されていないすべての権利を留保します。お客様は、当社の明示的な書面による許可がない限り、プラットフォームに含まれるコンテンツを使用、複製、または配布しないことに同意するものとします。</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>免責事項</h2>\n" +
    " <p>プラットフォームおよびすべてのサービスは、「現状有姿」、「利用可能な状態」、「すべての瑕疵を伴った状態」で提供されます。法律で認められる最大限の範囲において、302.AIは代理人、および当社の従業員、管理者、役員、譲受人、代理人は、明示的または黙示的を問わず、以下についていかなる表明、保証、または推奨も行いません。(1) プラットフォーム (2) プラットフォームを通じて提供される情報 (3) サービス (4) 302.AIへのまたは302.AIを介した情報の送信に関連するセキュリティ</p>\n" +
    " <p>さらに、当社は、商品性、特定目的への適合性、逸失利益、非侵害、所有権、カスタマイズ、取引、平穏享有権、システム統合、およびコンピュータウイルスの不存在に関する保証を含むがこれらに限定されない、明示的または黙示的なすべての保証を否認します。</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>責任の制限</h2>\n" +
    " <p>第三者によって当社のプラットフォームにまたは当社のプラットフォームを介して送信される場合、意図的または意図的でないサービスの障害または中断、または第三者の著作権者またはその他の知的財産権者に関連する行為。上記の責任の制限は、該当する法域の法律で認められる最大限の範囲で適用されるものとします。一部の法域では、付随的損害または結果的損害に対する責任の制限または免除が認められていないため、上記の責任の制限または免除がお客様に適用されない場合があります。具体的には、認められていない法域では、当社は以下の事項について責任を負いません。(1) 302.AIまたはその役員、従業員、または代理人の過失による死亡または人身傷害</p>\n" +
    " <p>(1) 詐欺的な不実表示 (2) 現在または将来において免除することが違法となる責任。完全な責任の免除が認められない場合、お客様は、当社がお客様に対して負う総責任が、お客様が当社のプラットフォーム（当社のサービスを含む）の利用に対して過去1か月間に支払った金額を超えないことに同意するものとします。</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>補償</h2>\n" +
    " <p>お客様は、以下に起因するあらゆる請求、損害賠償、義務、損失、責任、費用、負債、および経費（弁護士費用を含みますが、これに限定されません）について、当社を補償し、免責することに同意するものとします。· お客様による302.AIプロキシプラットフォーム（本サービスを含む）の使用およびアクセス。· お客様による本契約の条項への違反。· お客様による他のユーザーまたは第三者とのやり取り。· お客様による第三者の権利（著作権、財産権、契約上の権利を含みますが、これに限定されません）の侵害。この防御および補償義務は、本契約およびお客様による302.AIプロキシプラットフォームの使用期間終了後も存続します。お客様はまた、かかる請求に対して当社を防御する責任を負うことに同意するものとします。かかる請求が発生した場合、当社はお客様に当社の選定する弁護士費用の支払いを求める場合があります。お客様は、この補償が、お客様に当社の合理的な弁護士費用、訴訟費用、および経費の支払いを求めることにまで及ぶことに同意するものとします。この条項に違反した場合、当社は請求を主張する当事者との和解を選択することができ、お客様は当社が裁判を行った場合と同様に損害賠償責任を負うことになります。</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>著作権</h2>\n" +
    " <p>当社は著作権侵害を非常に深刻に受け止めています。お客様が所有するコンテンツが侵害されたと思われる場合は、以下の情報を含むメッセージをお送りください。·お客様のお名前。·著作権を侵害された当事者のお名前（お客様のお名前と異なる場合）。·侵害された作品の名称と説明。·当社のプラットフォームにおける侵害コピーの場所。·上記の著作物の使用が著作権者（または著作権者に代わって行動することを法的に認められた第三者）によって許可されておらず、かつ法律上使用が許可されていないとお客様が誠実に信じている旨の声明。·お客様が偽証罪の罰則を承知の上で、この通知に含まれる情報が正確であり、あなたが著作権者であるか、または法律に基づきその使用に関する侵害訴訟を提起する独占的権利を有することを宣誓します。この通知に署名し、あなたのメールアドレスを記載してください。送付先は、当社の著作権代理人である302.AIの著作権代理人です。異議申し立て通知 302.AIから、あなたが投稿したコンテンツが削除されたという通知を受け取った場合は、異議申し立て通知を提出することができます。異議申し立て通知には、以下の内容を含める必要があります。· あなたの氏名、住所、メールアドレス、または電子署名。· 通知参照番号（該当する場合）。· 削除される前の素材の識別情報と場所。· 偽証罪の罰則を承知の上で、素材が誤ってまたは誤認により削除されたという声明。· 削除通知を提出した当事者からの訴状送達の受領への同意。異議申し立て通知が上記の要件を厳密に遵守していない限り、当社は異議申し立て通知に対していかなる措置も講じない可能性があることにご注意ください。上記の削除通知の手順に従って、この通知を送信してください。異議申し立て通知。</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>解約</h2>\n" +
    " <p>お客様は、当社のカスタマーサポートに連絡することで、いつでも有料サブスクリプションまたはアカウントをキャンセルできます。払い戻しは、本契約に含まれる払い戻し条件に従います。アカウントの解約に伴い、当社のプラットフォームの一部へのアクセスが直ちに無効になる場合がありますのでご注意ください。当社は、以下のいずれかの場合に、お客様との本契約を解約することができます。</p>\n" +
    " <ul>\n" +
    " <li>お客様が当社のプラットフォームの利用において適用法に違反した場合。</li>\n" +
    " <li>お客様が本契約またはプラットフォームで許可されている条項に違反した場合。</li>\n" +
    " <li>お客様の行為が302.AIまたは当社の事業利益に法的損害を与える可能性があると当社が独自の裁量で判断した場合。解約の場合、当社は迅速な説明に努めますが、当社はそうする義務を負いません。</li>\n" +
    " </ul>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>完全合意</h2>\n" +
    " <p>本契約は、プライバシーポリシーと併せて、本契約の主題に関する両当事者間の完全かつ排他的な了解事項および合意を構成し、書面または口頭を問わず、本契約の主題に関する従前または同時のすべての合意または了解事項に優先します。本契約のいかなる条項の放棄、変更、または修正も、各当事者の正当に権限を与えられた代表者が署名した書面によってのみ有効となります。</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>修正契約</h2>\n" +
    " <p>当社は、本契約を随時変更することがあります。本契約を変更する場合、このページを更新し、最終変更日を記載するか、お客様にメールを送信することがあります。お客様は変更に同意しないこともできますが、その場合は、当社のプラットフォームおよび本サービスの利用を直ちに中止する必要があります。</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>当事者の関係</h2>\n" +
    " <p>当事者は独立した契約者です。本契約は、お客様と302.AIとの間に、パートナーシップ、フランチャイズ、ジョイントベンチャー、代理店、信託関係、または雇用関係を構築するものではありません。</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>不正使用防止</h2>\n" +
    " <p>お客様は、当社の不正使用防止ポリシー。当社のサービスを不正に使用しているユーザーを発見した場合は、カスタマーサポート（luna@302.AI**）までご連絡ください。</p>\n" +
    " </div>\n" +
    "\n" +
    " <div class=\"section\">\n" +
    " <h2>第三者コンテンツ</h2>\n" +
    " <p>当社は、お客様がプラットフォームのご利用中にアクセス、ダウンロード、受信、または購入するデータ、コンテンツ、サービス、または製品（ソフトウェアを含む）を管理しておらず、それらについて責任を負いません。当社は、プラットフォーム、当社のネットワーク、一般の人々、またはユーザーを保護するために、情報、送信、または特定の情報、サービス、製品、またはドメインへのアクセスをブロックする場合がありますが、その義務を負うものではありません。当社は、プラットフォームを通じてアクセスされる第三者コンテンツの発行者ではなく、プラットフォームを通じてアクセスされる可能性のある、第三者に提供される、または第三者によって提供される意見、アドバイス、声明、メッセージ、サービス、グラフィック、データ、またはその他の情報の内容、正確性、適時性、または配信について責任を負いません。 </p>\n" +
    " </div>\n" +
    " \n" +
    " <div class=\"section\">\n" +
    " <h2>地理的アクセス</h2>\n" +
    " <p>本プラットフォームは地理的に制限されていません。ただし、当社は、本プラットフォームがお客様の所在地および法域における使用またはアクセスに適していることを表明または保証するものではありません。お客様は、ご自身の判断で居住国において本プラットフォームにアクセスし、使用するものとし、適用される場合、かつその範囲において、現地の法律および規制の遵守について単独で責任を負うものとします。当社は、いつでも当社の独自の裁量により、本プラットフォームまたはその一部の利用を、個人、団体、地理的地域、または法域に制限する権利を留保します。</p>\n" +
    " </div>\n" +
    " \n" +
    " <div class=\"section\">\n" +
    " <h2>プラットフォームに関する問題とサポート</h2>\n" +
    " <p>本プラットフォームへのアクセスまたは使用中に問題が発生した場合は、プラットフォームに関するご質問は、カスタマーサポート（luna@302.AI）までお問い合わせください。</p>\n" +
    " </div>\n" +
    "</body>\n" +
    "</html>\n".trimIndent()
}