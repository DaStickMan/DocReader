package com.docreader.core.pdf

/** Problema encontrado em uma anotação. */
data class AnnotationIssue(val id: String, val reason: String)

/** Coleta problemas que impedem a persistência de uma anotação. Nunca lança. */
object AnnotationValidator {

    fun validate(annotations: List<Annotation>, pageCount: Int): List<AnnotationIssue> {
        val issues = mutableListOf<AnnotationIssue>()

        for (annotation in annotations) {
            if (annotation.id.isBlank()) {
                issues += AnnotationIssue(annotation.id, "id em branco")
            }
            if (annotation.page !in 0 until pageCount) {
                issues += AnnotationIssue(annotation.id, "página fora do intervalo: ${annotation.page}")
            }
            when (annotation) {
                is StickyNote -> if (annotation.text.isBlank()) {
                    issues += AnnotationIssue(annotation.id, "texto da nota em branco")
                }
                is Freehand -> {
                    if (annotation.points.size < 2) {
                        issues += AnnotationIssue(annotation.id, "traço precisa de ao menos 2 pontos")
                    }
                    if (annotation.width <= 0f) {
                        issues += AnnotationIssue(annotation.id, "largura deve ser maior que zero")
                    }
                }
                is Highlight -> Unit // rect já é validado no construtor
            }
        }
        return issues
    }
}
