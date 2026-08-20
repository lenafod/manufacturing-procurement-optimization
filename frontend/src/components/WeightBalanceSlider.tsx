interface WeightBalanceSliderProps {
  pricePercent: number;
  onChange: (pricePercent: number) => void;
}

// Klizač po konstrukciji uvek daje zbir 1 (cena% + rok%), tačno ono što backend zahteva
// (ProcurementOptimizationService.optimizeProcurement baca InvalidRequestException inače).
export function WeightBalanceSlider({ pricePercent, onChange }: WeightBalanceSliderProps) {
  const deliveryPercent = 100 - pricePercent;

  return (
    <div className="balance">
      <div className="balance-readout">
        <div className="balance-side price">
          <span className="k">Cena</span>
          <span className="v">{pricePercent}%</span>
        </div>
        <div className="balance-side delivery right">
          <span className="k">Rok isporuke</span>
          <span className="v">{deliveryPercent}%</span>
        </div>
      </div>
      <div className="split-bar">
        <div className="fill-price" style={{ width: `${pricePercent}%` }} />
        <div className="fill-delivery" style={{ width: `${deliveryPercent}%` }} />
      </div>
      <input
        type="range"
        className="balance-slider"
        min={0}
        max={100}
        value={pricePercent}
        onChange={(e) => onChange(Number(e.target.value))}
        aria-label="Odnos cene i roka isporuke"
      />
      <p className="balance-hint">Prevuci ulevo za prioritet ceni, udesno za prioritet brzini isporuke.</p>
    </div>
  );
}
