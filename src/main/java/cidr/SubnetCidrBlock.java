package cidr;

import io.vavr.collection.Stream;

import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * This is simple utility to create Classless Inter-Domain Routing(CIDR) blocks for given masterBlock
 * <p>
 * Algorithm based on https://en.wikipedia.org/wiki/Classless_Inter-Domain_Routing
 *
 * <p>
 * It only generates CIDR block with 256,128,64,32,16 IPs (i.e, subnet mask 24,25,26,27,28)
 * <p>
 * 1. Returns max number of given for given vcnRange
 * 2. Returns all the CIDR blocks
 * 3. Return CIDR blocks along with full IP list
 * </ol>
 *
 * @author tvajjala
 */
public class SubnetCidrBlock {

    /**
     * IPv4 uses 32-bit IP address
     */
    private static final int IPV4_MAX = 32;

    /**
     * Subnet mask
     */
    private String masterBlockIPV4;

    /**
     * VCN Mask
     */
    private Integer masterBlockMask;

    /**
     * Mask to split CIDR range
     */
    private Integer subnetMask;

    /**
     * @param masterBlockIPV4 masterBlockIPV4
     * @param masterBlockMask masterBlockMask
     * @param subnetMask      subnetMask
     */
    private SubnetCidrBlock(String masterBlockIPV4, Integer masterBlockMask, Integer subnetMask) {
        this.masterBlockIPV4 = masterBlockIPV4;
        this.masterBlockMask = masterBlockMask;
        this.subnetMask = subnetMask;
    }

    /**
     * Max Byte Capacity
     * <p>
     * IPV4 (X.X.X.X) address represents 4 block with each of 1 Byte (with range (0- 255) decimals)
     * <p>
     */
    private static final int MAX = ((int) Math.pow(2, 8) - 1);


    public Iterator<String> getSubnetCidrBlocks() {
        return new CIDRBlockHolder(masterBlockIPV4, subnetMask);
    }

    class CIDRBlockHolder implements Iterator<String> {

        private int[] blocks;
        private int maxIpCount;

        private int b0;
        private int b1;
        private int b2;
        private int b3;
        private boolean first;

        CIDRBlockHolder(String startingIp, int splitMask) {
            this.blocks = stream(startingIp.split("\\.")).mapToInt(Integer::parseInt).toArray();
            b0 = blocks[0];
            b1 = blocks[1];
            b2 = blocks[2];
            b3 = blocks[3];
            first = true;
            this.maxIpCount = (int) Math.pow(2, (IPV4_MAX - splitMask));
        }

        boolean hasNextInvoked = false;

        @Override
        public boolean hasNext() {
            hasNextInvoked = true;
            if (first) {
                first = false;
                return true;
            }
            increment();

            //return b0 <= MAX;// stop if 255.255.255.x
            return b2 <= MAX; // stop if x.x.255.x
        }

        @Override
        public String next() {
            if (!hasNextInvoked) {
                throw new IllegalStateException("Invalid state - must invoke hasNext before calling next");
            }
            hasNextInvoked = false;
            String subnet = String.format("%d.%d.%d.%d/%d", b0, b1, b2, b3, subnetMask);
            System.out.println("Initial IP " + String.format("%d.%d.%d.%d", b0, b1, b2, b3));
            System.out.println("Total IPs " + ipCount(subnetMask));
            System.out.println("Full " + getIPList(String.format("%d.%d.%d.%d", b0, b1, b2, b3), subnetMask));
            return subnet;
        }


        private void increment() {
            //TODO: bit shifting
            if ((b3 + maxIpCount) >= MAX) { //0+255, next b3 will be 0, and b2++
                b3 = 0;
                b2++;
            } else {
                b3 = (b3 + maxIpCount);
            }

         /*   if (b2 >= (MAX - 1)) {
                b2 = MAX;//max 255
                b1++;
            }

            if (b1 >= (MAX - 1)) {
                b2 = MAX;//max 255
                b1 = MAX;//max 255
                b0++;
            }*/
        }

    }


    private List<String> getFullIPList() {
        return getIPList(masterBlockIPV4, masterBlockMask);
    }

    private List<String> getIPList(String initialIP, int subnetMask) {
        final int[] bytes = stream(initialIP.split("\\.")).mapToInt(Integer::parseInt).toArray();
        int b3 = bytes[3];
        int max = b3 + ipCount(subnetMask);
        max = (max > MAX) ? MAX : (max);   // Utility only support 24,25,26,27,28 masks.
        return Stream.range(b3, max)
                .map(i -> String.format("%d.%d.%d.%d", bytes[0], bytes[1], bytes[2], i))
                .collect(toList());
    }


    /**
     * IP Count for the given mask
     *
     * @param subnetMask
     * @return total IP Count
     */
    private static int ipCount(int subnetMask) {
        return (int) Math.pow(2, (IPV4_MAX - subnetMask));
    }

    public int getTotalIPs() {
        return ipCount(masterBlockMask);
    }


    public int getTotalUsableIPs() {
        int totalIPs = getTotalIPs();
        return (totalIPs <= 2) ? totalIPs : (totalIPs - 2);
    }

    public static Builder builder(Protocol protocol) {
        return new Builder(protocol);
    }


    /**
     * Builder
     */
    public static class Builder {

        private String masterBlock;

        private Protocol protocol;

        private Integer subnetMask;

        public Builder(Protocol protocol) {

            this.protocol = protocol;
        }

        /**
         * VCN Subnet Range
         *
         * @param masterBlock masterBlock block
         * @return Builder
         */
        Builder fromMasterBlock(String masterBlock) {
            this.masterBlock = masterBlock;
            return this;
        }

        Builder withSubnetMask(Integer subnetMask) {
            if (24 < subnetMask && subnetMask > 28) {
                //Restrict CIDR Blocks to 256, 128, 64, 32,16
                throw new IllegalArgumentException("Only supports 24-28 range");
            }
            this.subnetMask = subnetMask;
            return this;
        }

        public SubnetCidrBlock build() {
            int slashIndex = masterBlock.indexOf("/");
            String masterBlockIPv4 = masterBlock.substring(0, slashIndex);
            Integer masterBlockMask = Integer.parseInt(masterBlock.substring(slashIndex + 1));
            if (protocol.isIPv4() && !isValidIPV4(masterBlockIPv4)) {
                throw new IllegalArgumentException("Invalid IPV4 range provided");
            }

            // Must be within (32 > subnetMask > masterBlockMask)
            if ((subnetMask > 32) || (masterBlockMask > subnetMask)) {
                throw new IllegalArgumentException("Invalid subnetMask value provided");
            }

            return new SubnetCidrBlock(masterBlockIPv4, masterBlockMask, subnetMask);
        }

        private boolean isValidIPV4(String ipv4) {
            if (ipv4.split("\\.").length != 4) {
                return false;
            }
            //validate if the input decimal block having values between 0,255
            return stream(ipv4.split("\\.")).map(Integer::parseInt).noneMatch(bit -> (0 < bit && bit > MAX));
        }

    }
}

enum Protocol {

    /**
     * Only Supports IPV4
     */
    IPV4;

    // IPV6;//FIXME: Doesn't support
    
    public boolean isIPv4() {
        return this == IPV4;
    }

}