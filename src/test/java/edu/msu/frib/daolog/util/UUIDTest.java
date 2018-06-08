package edu.msu.frib.daolog.util;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;

public class UUIDTest {

	public static void main (String[] args) {
		
		NoArgGenerator timeBasedGenerator = Generators.timeBasedGenerator();
		
		//Generate time based UUID
        UUID firstUUID = timeBasedGenerator.generate();
        System.out.printf("1. First UUID is : %s ", firstUUID.toString());
        System.out.printf("\n2. Timestamp of UUID is : %d ", firstUUID.timestamp());

        UUID secondUUID = timeBasedGenerator.generate();
        System.out.printf("\n3. Second UUID is :%s ", secondUUID.toString());
        System.out.printf("\n4. Timestamp of UUID is : %d ", secondUUID.timestamp());

        //Generate custom UUID from network interface
        timeBasedGenerator = Generators.timeBasedGenerator(EthernetAddress.fromInterface());
        UUID customUUID = timeBasedGenerator.generate();
        UUID anotherCustomUUID = timeBasedGenerator.generate();
        UUID anotherCustomUUID2 = timeBasedGenerator.generate();

        System.out.printf("\n5. Custom UUID is :%s ", customUUID.toString());
        System.out.printf("\n6. Another custom UUID : %s ", anotherCustomUUID.toString());
        System.out.printf("\n6. Another custom UUID : %s ", anotherCustomUUID2.toString());
		
		
		
		
		UUID uuidType1 = timeBasedGenerator.generate();
        System.out.printf("1. First UUID is : %s ", uuidType1.toString());
        System.out.printf("\n2. Timestamp of UUID is : %d ", uuidType1.timestamp());
		System.out.println("uuidType1 version: " + uuidType1.version());
		System.out.println("uuidType1 variant: " + uuidType1.variant());
		System.out.println("uuidType1 node: " + uuidType1.node());
		System.out.println("uuidType1 getMostSignificantBits: " + uuidType1.getMostSignificantBits());
		System.out.println("uuidType1 getLeastSignificantBits: " + uuidType1.getLeastSignificantBits());
		
        
		UUID uuidType4 = UUID.randomUUID();
		System.out.println("uuidType4: " + uuidType4);
		System.out.println("uuidType4 version: " + uuidType4.version());
		System.out.println("uuidType4 variant: " + uuidType4.variant());
		System.out.println("uuidType4 node: " + uuidType4.node());
		System.out.println("uuidType4 getMostSignificantBits: " + uuidType4.getMostSignificantBits());
		System.out.println("uuidType4 getLeastSignificantBits: " + uuidType4.getLeastSignificantBits());
		
		Date now = new Date();
		UUID uuidType3 = UUID.fromString("35.9.58.201" + now.getTime());
		System.out.println("uuidType3: " + uuidType3);
	}
}
