package io.tolgee.controllers.internal.e2e_data

import io.swagger.v3.oas.annotations.Hidden
import io.tolgee.development.testDataBuilder.builders.TestDataBuilder
import io.tolgee.development.testDataBuilder.data.FormerUserTestData
import io.tolgee.development.testDataBuilder.data.NamespacesTestData
import io.tolgee.security.InternalController
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = ["*"])
@Hidden
@RequestMapping(value = ["internal/e2e-data/namespaces"])
@Transactional
@InternalController
class NamespacesE2eDataController : AbstractE2eDataController() {
  override val testData: TestDataBuilder
    get() = NamespacesTestData().root
}
