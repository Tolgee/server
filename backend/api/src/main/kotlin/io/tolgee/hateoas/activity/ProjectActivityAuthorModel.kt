package io.tolgee.hateoas.activity

import io.tolgee.dtos.Avatar
import org.springframework.hateoas.RepresentationModel

data class ProjectActivityAuthorModel(
  val id: Long,
  val username: String?,
  var name: String?,
  var avatar: Avatar?,
  var deleted: Boolean
) : RepresentationModel<ProjectActivityAuthorModel>()
