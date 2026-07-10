// Point d'entrée "web component" : enregistre <smartesn-intake> comme custom element,
// utilisable sur n'importe quel site (hors Angular).
// Build : `npm run build:elements` → dist/elements/
import { createApplication } from '@angular/platform-browser';
import { createCustomElement } from '@angular/elements';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { DemandeForm } from './app/features/demande/demande-form';

createApplication({
  providers: [provideHttpClient(withFetch())],
}).then((appRef) => {
  const element = createCustomElement(DemandeForm, { injector: appRef.injector });
  if (!customElements.get('smartesn-intake')) {
    customElements.define('smartesn-intake', element);
  }
});
