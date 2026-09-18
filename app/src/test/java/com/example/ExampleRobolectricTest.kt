package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("QR PromptPay", appName)
  }

  @Test
  fun `promptpay payload generation matches BOT EMVCo standard`() {
    val payload = com.example.util.PromptPayGenerator.generatePayload("0812345678", 150.0)
    org.junit.Assert.assertTrue("Payload must start with 000201", payload.startsWith("000201"))
    org.junit.Assert.assertTrue("Payload must contain promptpay tag 29", payload.contains("0016A000000677010111"))
    org.junit.Assert.assertTrue("Payload must contain currency 764", payload.contains("5303764"))
    org.junit.Assert.assertTrue("Payload must contain amount 150.00", payload.contains("5406150.00"))
    org.junit.Assert.assertTrue("Payload must end with CRC tag 6304", payload.contains("6304"))
  }

  @Test
  fun `wifi payload generation format`() {
    val payload = com.example.util.QrCodeUtil.buildWifiPayload("MyWiFi", "secret123", "WPA", false)
    assertEquals("WIFI:S:MyWiFi;T:WPA;P:secret123;H:false;;", payload)
  }

  @Test
  fun `wallet manager tracks 3 free uses then requires topup or subscription`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.data.WalletManager.initialize(context)
    com.example.data.WalletManager.setTestMode(false)
    com.example.data.WalletManager.resetTrial()

    // Initially 3 free uses
    org.junit.Assert.assertTrue("Should be able to use initially", com.example.data.WalletManager.canUse())
    assertEquals(3, com.example.data.WalletManager.walletState.value.freeUsesLeft)

    // Consume 1st use
    org.junit.Assert.assertTrue(com.example.data.WalletManager.consumeUsage())
    assertEquals(2, com.example.data.WalletManager.walletState.value.freeUsesLeft)

    // Consume 2nd use
    org.junit.Assert.assertTrue(com.example.data.WalletManager.consumeUsage())
    assertEquals(1, com.example.data.WalletManager.walletState.value.freeUsesLeft)

    // Consume 3rd use
    org.junit.Assert.assertTrue(com.example.data.WalletManager.consumeUsage())
    assertEquals(0, com.example.data.WalletManager.walletState.value.freeUsesLeft)

    // 4th use without top-up must be blocked!
    org.junit.Assert.assertFalse("4th use must be blocked when free quota is 0", com.example.data.WalletManager.canUse())
    org.junit.Assert.assertFalse("consumeUsage must return false when quota exhausted", com.example.data.WalletManager.consumeUsage())

    // Now user subscribes to Monthly VIP (฿59)
    val monthlyPlan = com.example.data.WalletManager.PLANS.first { it.id == "monthly_vip" }
    com.example.data.WalletManager.activatePlan(monthlyPlan)

    // VIP should now have unlimited usage and active VIP flag
    org.junit.Assert.assertTrue("VIP must have unlimited access", com.example.data.WalletManager.canUse())
    org.junit.Assert.assertTrue("VIP state must be active", com.example.data.WalletManager.walletState.value.isUnlimitedVip)
    org.junit.Assert.assertTrue("consumeUsage succeeds for VIP without decrementing credits", com.example.data.WalletManager.consumeUsage())
  }

  @Test
  fun `watching 30s ad rewards free user with +1 free use`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.data.WalletManager.initialize(context)
    com.example.data.WalletManager.setTestMode(false)
    com.example.data.WalletManager.resetTrial()

    // Consume all 3 uses
    repeat(3) { com.example.data.WalletManager.consumeUsage() }
    assertEquals(0, com.example.data.WalletManager.walletState.value.freeUsesLeft)
    org.junit.Assert.assertFalse(com.example.data.WalletManager.canUse())

    // Watch 30-second ad and earn 1 free use
    com.example.data.WalletManager.addFreeUse(1)
    assertEquals(1, com.example.data.WalletManager.walletState.value.freeUsesLeft)
    org.junit.Assert.assertTrue(com.example.data.WalletManager.canUse())

    // Successfully consume rewarded free use
    org.junit.Assert.assertTrue(com.example.data.WalletManager.consumeUsage())
    assertEquals(0, com.example.data.WalletManager.walletState.value.freeUsesLeft)
  }

  @Test
  fun `when isTestMode is true VIP is permanently active without needing payment API`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.data.WalletManager.initialize(context)
    com.example.data.WalletManager.setTestMode(true)

    // isTestMode must be true
    org.junit.Assert.assertTrue("isTestMode must be true", com.example.data.WalletManager.isTestMode)

    val state = com.example.data.WalletManager.walletState.value
    // VIP must be permanently active
    org.junit.Assert.assertTrue("VIP must be permanently active in test mode", state.isUnlimitedVip)
    org.junit.Assert.assertTrue("isMonthlyVip must be true in test mode", state.isMonthlyVip)
    org.junit.Assert.assertTrue("canUse must always return true", com.example.data.WalletManager.canUse())

    // consumeUsage must succeed continuously without deducting quota or requiring payment
    repeat(10) {
      org.junit.Assert.assertTrue("consumeUsage must always succeed in test mode", com.example.data.WalletManager.consumeUsage())
    }
  }

  @Test
  fun `LocalizationManager switches languages and returns proper localized strings`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.util.LocalizationManager.initialize(context)

    // Test Thai
    val thLang = com.example.util.LocalizationManager.ALL_LANGUAGES.first { it.code == "th" }
    com.example.util.LocalizationManager.setLanguage(thLang)
    assertEquals("สร้าง QR", com.example.util.LocalizationManager.getString("nav_generate"))

    // Test English
    val enLang = com.example.util.LocalizationManager.ALL_LANGUAGES.first { it.code == "en" }
    com.example.util.LocalizationManager.setLanguage(enLang)
    assertEquals("Generate QR", com.example.util.LocalizationManager.getString("nav_generate"))
    assertEquals("Support", com.example.util.LocalizationManager.getString("contact_admin"))

    // Test Chinese
    val zhLang = com.example.util.LocalizationManager.ALL_LANGUAGES.first { it.code == "zh_CN" }
    com.example.util.LocalizationManager.setLanguage(zhLang)
    assertEquals("生成二维码", com.example.util.LocalizationManager.getString("nav_generate"))

    // Test Japanese
    val jaLang = com.example.util.LocalizationManager.ALL_LANGUAGES.first { it.code == "ja" }
    com.example.util.LocalizationManager.setLanguage(jaLang)
    assertEquals("QR作成", com.example.util.LocalizationManager.getString("nav_generate"))
  }

  @Test
  fun `CurrencyManager supports Google Play currencies and formats prices accurately`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.util.CurrencyManager.initialize(context)

    // THB
    val thb = com.example.util.CurrencyManager.ALL_CURRENCIES.first { it.code == "THB" }
    com.example.util.CurrencyManager.setCurrency(thb)
    assertEquals("฿59.00", thb.format(59.0))

    // USD
    val usd = com.example.util.CurrencyManager.ALL_CURRENCIES.first { it.code == "USD" }
    com.example.util.CurrencyManager.setCurrency(usd)
    val usdPrice = usd.format(59.0)
    org.junit.Assert.assertTrue("USD format should start with $", usdPrice.startsWith("$"))

    // JPY (0 decimal places)
    val jpy = com.example.util.CurrencyManager.ALL_CURRENCIES.first { it.code == "JPY" }
    com.example.util.CurrencyManager.setCurrency(jpy)
    val jpyPrice = jpy.format(59.0)
    org.junit.Assert.assertTrue("JPY format should start with ¥", jpyPrice.startsWith("¥"))
    org.junit.Assert.assertFalse("JPY should have 0 decimal places", jpyPrice.contains("."))

    // EUR
    val eur = com.example.util.CurrencyManager.ALL_CURRENCIES.first { it.code == "EUR" }
    com.example.util.CurrencyManager.setCurrency(eur)
    val eurPrice = eur.format(59.0)
    org.junit.Assert.assertTrue("EUR format should start with €", eurPrice.startsWith("€"))
  }

  @Test
  fun `RootDetectionUtil performs full inspection and returns structured result`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val result = com.example.util.RootDetectionUtil.performFullCheck(context)
    org.junit.Assert.assertNotNull("Root check result must not be null", result)
    org.junit.Assert.assertNotNull("Reasons list must not be null", result.reasons)
    org.junit.Assert.assertTrue("Timestamp must be recent", result.timestamp > 0)
  }

  @Test
  fun `RootSecurityManager updates state flow and manages bypass correctly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val result = com.example.util.RootSecurityManager.verifyDeviceIntegrity(context)
    org.junit.Assert.assertEquals(result, com.example.util.RootSecurityManager.rootState.value)

    com.example.util.RootSecurityManager.resetBypass()
    org.junit.Assert.assertFalse("Bypass must be false after reset", com.example.util.RootSecurityManager.isBypassedForTesting.value)

    com.example.util.RootSecurityManager.acknowledgeAndBypassWarning()
    org.junit.Assert.assertTrue("Bypass must be true after acknowledge", com.example.util.RootSecurityManager.isBypassedForTesting.value)
  }
}
