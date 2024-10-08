import io.github.goquati.kotlin.util.Failure
import io.github.goquati.kotlin.util.Success
import io.github.goquati.kotlin.util.coroutine.*
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlinx.coroutines.flow.toList

class ResultUtilTest {

    @Test
    fun testFilter(): TestResult = runTest {
        val data = flowOf(
            Failure(1),
            Success("2"),
            Failure(3),
            Success("4"),
            Failure(5),
            Success("6"),
        )
        data.filterSuccess().toList() shouldBe listOf("2", "4", "6")
        data.filterFailure().toList() shouldBe listOf(1, 3, 5)

        val errors = mutableSetOf<Int>()
        data.filterSuccess(errorHandler = { errors.add(it) }).toList() shouldBe listOf("2", "4", "6")
        errors shouldBe setOf(1, 3, 5)
    }

    @Test
    fun testToCollection(): TestResult = runTest {
        val data1 = flowOf(
            Success("1"),
            Failure(2),
            Success("3"),
        )
        val data2 = flowOf(
            Success("1"),
            Success("2"),
            Success("3"),
        )
        data1.toResultList().failure shouldBe 2
        data2.toResultList().success shouldBe listOf("1", "2", "3")

        data1.toResultSet().failure shouldBe 2
        data2.toResultSet().success shouldBe setOf("1", "2", "3")
    }

    @Test
    fun testToCollectionOr(): TestResult = runTest {
        val data1 = flowOf(
            Success("1"),
            Failure(2),
            Success("3"),
        )
        val data2 = flowOf(
            Success("1"),
            Success("2"),
            Success("3"),
        )
        data1.toResultListOr {
            it shouldBe listOf(2)
            listOf("4")
        } shouldBe listOf("4")
        data1.toResultSetOr {
            it shouldBe listOf(2)
            setOf("4")
        } shouldBe setOf("4")
        data2.toResultListOr { throw Exception() } shouldBe listOf("1", "2", "3")
        data2.toResultSetOr { throw Exception() } shouldBe setOf("1", "2", "3")
    }
}