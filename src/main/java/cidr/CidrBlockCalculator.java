package cidr;


import java.util.Iterator;

/**
 * https://mxtoolbox.com/subnetcalculator.aspx
 */
public class CidrBlockCalculator {


    public static void main(String[] args) {

        SubnetCidrBlock subnetCidrBlock = SubnetCidrBlock.builder(Protocol.IPV4)
                .fromMasterBlock("10.0.128.0/17")//<-- vcnSubnet
                .withSubnetMask(24)
                .build();
        Iterator<String> iterator = subnetCidrBlock.getSubnetCidrBlocks();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }


        System.out.println(subnetCidrBlock.getTotalIPs());
        System.out.println(subnetCidrBlock.getTotalUsableIPs());


      /*  cidrBlock.getCIDRBlockWithFullIPList().entrySet().forEach(stringListEntry -> {

            System.out.println("----- " + stringListEntry.getKey() + "  ----- ");
            System.out.print(stringListEntry.getValue());
            System.out.println();
            System.out.println();


        });*/

    }


}
