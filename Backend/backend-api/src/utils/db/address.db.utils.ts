import type { DrizzleDb } from '#/drizzle/drizzle.service.js';
import { cities, directions, provinces } from '#/generated/drizzle/schema.js';
import { eq, and } from 'drizzle-orm';
import { BadRequestException, NotFoundException } from '@nestjs/common';
import { AddressType } from '#/generated/drizzle/enums.js';

export async function checkAddressIsValid(
  cityId: number,
  provinceId: number,
  countryIso: number,
  tx: DrizzleDb,
) {
  const [validAddress] = await tx
    .select({ cityId: cities.id })
    .from(cities)
    .innerJoin(provinces, eq(cities.province_id, provinces.id))
    .where(
      and(
        eq(cities.id, cityId),
        eq(cities.province_id, provinceId),
        eq(provinces.country_iso, countryIso),
      ),
    )
    .limit(1);

  if (!validAddress) {
    throw new BadRequestException('Invalid address hierarchy');
  }
}

export async function checkAddressIsValidForPatch(
  userId: string,
  tx: DrizzleDb,
  cityId?: number,
  provinceId?: number,
  countryIso?: number,
): Promise<{
  addressId: bigint;
  countryIso: number;
  provinceId: number;
  cityId: number;
}> {
  const [currentAddress] = await tx
    .select({
      id: directions.id,
      country_iso: directions.country_iso,
      province_id: directions.province_id,
      city_id: directions.city_id,
    })
    .from(directions)
    .where(
      and(
        eq(directions.user_id, userId),
        eq(directions.type, AddressType.STORE),
      ),
    )
    .limit(1)
    .for('update');

  if (!currentAddress) {
    throw new NotFoundException('User address not found');
  }

  const mergedAddress = {
    addressId: currentAddress.id,
    countryIso: countryIso ?? currentAddress.country_iso,
    provinceId: provinceId ?? currentAddress.province_id,
    cityId: cityId ?? currentAddress.city_id,
  };

  await checkAddressIsValid(
    mergedAddress.cityId,
    mergedAddress.provinceId,
    mergedAddress.countryIso,
    tx,
  );

  return mergedAddress;
}
