interface WeightStepperProps {
  pricePercent: number;
  onChange: (pricePercent: number) => void;
  step?: number;
}

// dva polja uvek saberu 100% po konstrukciji, tacno ono sto backend zahteva
// (ProcurementOptimizationService.optimizeProcurement baca InvalidRequestException inace).
export function WeightStepper({ pricePercent, onChange, step = 10 }: WeightStepperProps) {
  const deliveryPercent = 100 - pricePercent;

  const setPrice = (value: number) => onChange(Math.min(100, Math.max(0, value)));

  return (
    <div className="balance">
      <div className="stepper-row">
        <span className="stepper-label">Cena</span>
        <div className="stepper-control">
          <button type="button" className="stepper-btn" disabled={pricePercent <= 0} onClick={() => setPrice(pricePercent - step)}>
            −
          </button>
          <span className="stepper-value">{pricePercent}%</span>
          <button type="button" className="stepper-btn" disabled={pricePercent >= 100} onClick={() => setPrice(pricePercent + step)}>
            +
          </button>
        </div>
      </div>
      <div className="stepper-row">
        <span className="stepper-label">Rok isporuke</span>
        <div className="stepper-control">
          <button
            type="button"
            className="stepper-btn"
            disabled={deliveryPercent <= 0}
            onClick={() => setPrice(pricePercent + step)}
          >
            −
          </button>
          <span className="stepper-value">{deliveryPercent}%</span>
          <button
            type="button"
            className="stepper-btn"
            disabled={deliveryPercent >= 100}
            onClick={() => setPrice(pricePercent - step)}
          >
            +
          </button>
        </div>
      </div>
      <div className="split-bar" style={{ marginTop: '0.8rem', marginBottom: 0 }}>
        <div className="fill-price" style={{ width: `${pricePercent}%` }} />
        <div className="fill-delivery" style={{ width: `${deliveryPercent}%` }} />
      </div>
    </div>
  );
}
