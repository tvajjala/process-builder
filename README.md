# process-builder

Contains CIDR Block Builder Utility

```java

package cidr;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

/**
 * @author tvajjala
 */
public class CidrBlockBuilderTest {

    @Test
    void subnetBlockWithMask24Test() {

        //given:
        SubnetCidrBlock networkSubnet = SubnetCidrBlock.builder(Protocol.IPV4)
                .fromMasterBlock("10.0.128.0/17")//<-- vcnSubnet
                .withSubnetMask(24)
                .build();

        //when:
        Iterator<String> iterator = networkSubnet.getSubnetCidrBlocks();

        //then: with 256 IP blocks
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.128.0/24", iterator.next());

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.129.0/24", iterator.next());

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.130.0/24", iterator.next());

    }


    @Test
    void subnetBlockWithMask25Test() {

        //given: scenario
        SubnetCidrBlock networkSubnet = SubnetCidrBlock.builder(Protocol.IPV4)
                .fromMasterBlock("10.0.128.0/17")//<-- vcnSubnet
                .withSubnetMask(25)// any range between 24-28
                .build();

        //when: invoke build cidr block
        Iterator<String> iterator = networkSubnet.getSubnetCidrBlocks();

        //then: expect 128 IP slots
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.128.0/25", iterator.next());

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.128.128/25", iterator.next());

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.129.0/25", iterator.next());

    }
}

```

