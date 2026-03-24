export function HowItWorks() {
  const steps = [
    { step: 1, title: 'Đăng ký tài khoản', desc: 'Cung cấp thông tin cơ bản.' },
    { step: 2, title: 'Tạo yêu cầu mới', desc: 'Mô tả tình huống, đính kèm ảnh, chọn vị trí.' },
    { step: 3, title: 'Nhận hỗ trợ', desc: 'Đội cứu hộ được điều phối và thông báo cho bạn.' },
  ];

  return (
    <section className="text-center">
      <h2 className="text-3xl font-bold mb-8">Cách thức hoạt động</h2>
      <div className="flex flex-wrap justify-center gap-8">
        {steps.map((s) => (
          <div key={s.step} className="w-64">
            <div className="bg-red-100 rounded-full w-12 h-12 flex items-center justify-center mx-auto mb-3 text-red-500 font-bold">
              {s.step}
            </div>
            <h4 className="font-semibold">{s.title}</h4>
            <p className="text-sm">{s.desc}</p>
          </div>
        ))}
      </div>
    </section>
  );
}