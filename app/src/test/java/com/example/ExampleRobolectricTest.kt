package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.ComplianceEngine
import com.example.model.ProductCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("INSPECTRA", appName)
  }

  @Test
  fun `evaluate detergent sample compliance`() {
    val (score, violations) = ComplianceEngine.evaluateCompliance(
      productName = "ABC Detergent",
      category = ProductCategory.HOUSEHOLD_PRODUCTS,
      fields = emptyList(),
      evidenceImageName = "img_detergent_package"
    )
    val status = ComplianceEngine.determineStatus(score, violations)
    assertNotNull(violations)
    assertEquals(true, violations.isNotEmpty())
    assertEquals(com.example.model.ComplianceStatus.NON_COMPLIANT, status)
  }
}

