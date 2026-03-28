import { useState, useCallback } from 'react';

export function useFileUpload(maxFiles: number = 5) {
  const [files, setFiles] = useState<File[]>([]);
  const [previews, setPreviews] = useState<string[]>([]);

  const addFiles = useCallback((newFiles: FileList) => {
    const fileArray = Array.from(newFiles);
    const availableSlots = maxFiles - files.length;
    const filesToAdd = fileArray.slice(0, availableSlots);

    if (filesToAdd.length < fileArray.length) {
      // Có thông báo nếu vượt quá số lượng cho phép (tùy chọn)
    }

    const newPreviews = filesToAdd.map(file => URL.createObjectURL(file));

    setFiles(prev => [...prev, ...filesToAdd]);
    setPreviews(prev => [...prev, ...newPreviews]);
  }, [files.length, maxFiles]);

  const removeFile = useCallback((index: number) => {
    setFiles(prev => prev.filter((_, i) => i !== index));
    setPreviews(prev => {
      // Giải phóng bộ nhớ cho URL
      URL.revokeObjectURL(prev[index]);
      return prev.filter((_, i) => i !== index);
    });
  }, []);

  const clearFiles = useCallback(() => {
    previews.forEach(url => URL.revokeObjectURL(url));
    setFiles([]);
    setPreviews([]);
  }, [previews]);

  return { files, previews, addFiles, removeFile, clearFiles };
}
