package nl.kataru.pvdata.domain

import org.junit.Test
import kotlin.test.assertEquals

/**
 * Created by morten on 2-1-17.
 */

class DomainTest {
    @Test fun testInverter() {
        with( Inverter("serial", "Omnik", "2000K", 2000) )
        {
            assertEquals("serial", serialNumber)
            assertEquals("Omnik", brand)
            assertEquals("2000K", type)
            assertEquals(2000, ratedPower)
        }
    }
}