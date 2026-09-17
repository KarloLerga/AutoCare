package hr.unizd.autocare.controller;

import hr.unizd.autocare.model.Data.ServiceInput;

/** Mali listener za spremanje servisne forme u razlicitim ekranima. */
public interface ServiceEditorListener {
  void saveService(ServiceInput serviceInput);
}
