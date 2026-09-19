package hr.unizd.autocare.controller;

import hr.unizd.autocare.model.Data.ServiceInput;

/** Mali listener za spremanje servisne forme na različitim ekranima. */
public interface ServiceEditorListener {
  void saveService(ServiceInput serviceInput);
}
