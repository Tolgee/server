package io.tolgee.api.v2.controllers

import io.swagger.v3.oas.annotations.tags.Tag
import io.tolgee.component.reporting.BusinessEventPublisher
import io.tolgee.component.reporting.OnBusinessEventToCaptureEvent
import io.tolgee.hateoas.quickStart.QuickStartModel
import io.tolgee.hateoas.quickStart.QuickStartModelAssembler
import io.tolgee.security.authentication.AuthenticationFacade
import io.tolgee.service.QuickStartService
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v2/quick-start")
@Tag(name = "Quick start")
class QuickStartController(
  private val quickStartService: QuickStartService,
  private val quickStartModelAssembler: QuickStartModelAssembler,
  private val authenticationFacade: AuthenticationFacade,
  private val businessEventPublisher: BusinessEventPublisher,
) {
  @PutMapping("/steps/{step}/complete")
  fun completeGuideStep(
    @PathVariable("step") step: String,
  ): QuickStartModel {
    val entity =
      quickStartService.completeStep(authenticationFacade.authenticatedUser, step)
        ?: throw NotFoundException()
    businessEventPublisher.publish(
      OnBusinessEventToCaptureEvent(
        eventName = "QUICK_START_STEP_COMPLETED",
        userAccountDto = authenticationFacade.authenticatedUser,
      ),
    )
    return quickStartModelAssembler.toModel(entity)
  }

  @PutMapping("/set-finished/{finished}")
  fun setFinishedState(
    @PathVariable finished: Boolean,
  ): QuickStartModel {
    val entity = quickStartService.setFinishState(authenticationFacade.authenticatedUser, finished)
    businessEventPublisher.publish(
      OnBusinessEventToCaptureEvent(
        eventName = "QUICK_START_FINISHED",
        userAccountDto = authenticationFacade.authenticatedUser,
      ),
    )
    return quickStartModelAssembler.toModel(entity)
  }

  @PutMapping("/set-open/{open}")
  fun setOpenState(
    @PathVariable open: Boolean,
  ): QuickStartModel {
    val entity = quickStartService.setOpenState(authenticationFacade.authenticatedUser, open)
    return quickStartModelAssembler.toModel(entity)
  }
}
