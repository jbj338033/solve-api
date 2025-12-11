package kr.solve.domain.file.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kr.solve.domain.file.application.service.FileService
import kr.solve.domain.file.presentation.request.CreatePresignedUrlRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "File", description = "파일 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/files")
class FileController(
    private val fileService: FileService,
) {
    @Operation(summary = "Presigned URL 생성")
    @PostMapping("/presigned")
    fun createPresignedUrl(
        @Valid @RequestBody request: CreatePresignedUrlRequest,
    ) = fileService.createPresignedUrl(request)
}
