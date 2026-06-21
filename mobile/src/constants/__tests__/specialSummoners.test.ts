import {
  GAS_MASK_PUUID,
  hasGasMaskEasterEgg,
} from '../../constants/specialSummoners';

describe('specialSummoners', () => {
  it('hasGasMaskEasterEgg только для заданного PUUID', () => {
    expect(hasGasMaskEasterEgg(GAS_MASK_PUUID)).toBe(true);
    expect(hasGasMaskEasterEgg('other-puuid')).toBe(false);
    expect(hasGasMaskEasterEgg(null)).toBe(false);
  });
});
