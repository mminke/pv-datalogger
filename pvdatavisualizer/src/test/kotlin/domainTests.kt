package nl.kataru.pvdata.domain

import nl.kataru.pvdata.core.Inverter
import org.junit.Test
import kotlin.test.assertEquals


class DomainTest {
    @Test fun testInverter() {
        with(Inverter("id1", "serial", "Omnik", "2000K", 2000, "accountid1"))
        {
            assertEquals("id1", id)
            assertEquals("serial", serialNumber)
            assertEquals("Omnik", brand)
            assertEquals("2000K", type)
            assertEquals(2000, ratedPower)
        }
    }
}