/** Пасхалка на аватаре — отображается только для заданного PUUID. */
export const GAS_MASK_PUUID =
  'rBNuosGannrgprUnrw_dalxGi5apTKlB91LMog6uTsZUWFuNHdOzvA2iZ5qOSyfXWkMQCvBlR7k1Kg';

export function hasGasMaskEasterEgg(puuid: string | null | undefined): boolean {
  return puuid === GAS_MASK_PUUID;
}
