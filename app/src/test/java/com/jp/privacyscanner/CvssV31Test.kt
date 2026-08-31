package com.jp.privacyscanner

import com.jp.privacyscanner.data.bugbounty.CvssV31
import com.jp.privacyscanner.data.bugbounty.CvssV31.AttackComplexity
import com.jp.privacyscanner.data.bugbounty.CvssV31.AttackVector
import com.jp.privacyscanner.data.bugbounty.CvssV31.Impact
import com.jp.privacyscanner.data.bugbounty.CvssV31.Metrics
import com.jp.privacyscanner.data.bugbounty.CvssV31.PrivilegesRequired
import com.jp.privacyscanner.data.bugbounty.CvssV31.Scope
import com.jp.privacyscanner.data.bugbounty.CvssV31.UserInteraction
import org.junit.Assert.assertEquals
import org.junit.Test

class CvssV31Test {

    @Test
    fun `critical 9_8 - AV N AC L PR N UI N S U C H I H A H`() {
        val m = Metrics(
            AttackVector.NETWORK, AttackComplexity.LOW, PrivilegesRequired.NONE,
            UserInteraction.NONE, Scope.UNCHANGED, Impact.HIGH, Impact.HIGH, Impact.HIGH
        )
        assertEquals(9.8, CvssV31.baseScore(m), 0.001)
        assertEquals("Crítica", CvssV31.severityLabel(CvssV31.baseScore(m)))
    }

    @Test
    fun `xss refletido 6_1 - scope changed`() {
        val m = Metrics(
            AttackVector.NETWORK, AttackComplexity.LOW, PrivilegesRequired.NONE,
            UserInteraction.REQUIRED, Scope.CHANGED, Impact.LOW, Impact.LOW, Impact.NONE
        )
        assertEquals(6.1, CvssV31.baseScore(m), 0.001)
    }

    @Test
    fun `priv esc local 7_8`() {
        val m = Metrics(
            AttackVector.LOCAL, AttackComplexity.LOW, PrivilegesRequired.LOW,
            UserInteraction.NONE, Scope.UNCHANGED, Impact.HIGH, Impact.HIGH, Impact.HIGH
        )
        assertEquals(7.8, CvssV31.baseScore(m), 0.001)
    }

    @Test
    fun `sem impacto e zero`() {
        val m = Metrics(
            AttackVector.NETWORK, AttackComplexity.LOW, PrivilegesRequired.NONE,
            UserInteraction.NONE, Scope.UNCHANGED, Impact.NONE, Impact.NONE, Impact.NONE
        )
        assertEquals(0.0, CvssV31.baseScore(m), 0.001)
        assertEquals("Nenhuma", CvssV31.severityLabel(0.0))
    }

    @Test
    fun `vetor canonico`() {
        val m = Metrics(
            AttackVector.NETWORK, AttackComplexity.LOW, PrivilegesRequired.NONE,
            UserInteraction.NONE, Scope.UNCHANGED, Impact.HIGH, Impact.HIGH, Impact.HIGH
        )
        assertEquals("CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H", CvssV31.vector(m))
    }

    @Test
    fun `privilegios requeridos dependem do scope`() {
        assertEquals(0.62, PrivilegesRequired.LOW.score(scopeChanged = false), 0.0001)
        assertEquals(0.68, PrivilegesRequired.LOW.score(scopeChanged = true), 0.0001)
    }
}
